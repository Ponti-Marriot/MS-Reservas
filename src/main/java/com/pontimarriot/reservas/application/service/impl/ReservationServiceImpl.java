package com.pontimarriot.reservas.application.service.impl;

import com.pontimarriot.reservas.api.dto.*;
import com.pontimarriot.reservas.application.service.ReservationService;
import com.pontimarriot.reservas.domain.enums.PaymentStatus;
import com.pontimarriot.reservas.domain.enums.ReservationStatus;
import com.pontimarriot.reservas.domain.model.AuditLog;
import com.pontimarriot.reservas.domain.model.Payment;
import com.pontimarriot.reservas.domain.model.Reservation;
import com.pontimarriot.reservas.infrastructure.dbclient.DbClient;
import com.pontimarriot.reservas.infrastructure.kafka.ReservationEventPublisher;
import com.pontimarriot.reservas.infrastructure.properties.PropertiesClient;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final DbClient dbClient;
    private final PropertiesClient propertiesClient;
    private final ReservationEventPublisher eventPublisher;
    
    // Simulación simple de idempotencia (en producción usar Redis o DB)
    private final Map<String, String> idempotencyStore = new ConcurrentHashMap<>();

    public ReservationServiceImpl(DbClient dbClient, 
                                  PropertiesClient propertiesClient,
                                  ReservationEventPublisher eventPublisher) {
        this.dbClient = dbClient;
        this.propertiesClient = propertiesClient;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public AvailabilitySearchResponse searchAvailability(
            AvailabilitySearchRequest request, String correlationId) {
        // 1. Validar fechas
        validateDates(request.checkIn(), request.checkOut());
        
        // 2. Llamar a MS-Propiedades para ver habitaciones disponibles
        propertiesClient.isRoomAvailable(
            request.roomTypeCode(), 
            request.checkIn(), 
            request.checkOut(), 
            request.numRooms()
        );
        
        // 3. Calcular precio total y devolver respuesta
        // Por ahora, respuesta simulada
        String searchId = UUID.randomUUID().toString();
        return new AvailabilitySearchResponse(searchId, List.of());
    }

    @Override
    public CreateReservationResponse createReservation(
            CreateReservationRequest request,
            String idempotencyKey,
            String correlationId) {

        // 0. Verificar idempotencia: si ya existe una reserva para esa key, devolverla
        if (idempotencyStore.containsKey(idempotencyKey)) {
            String existingId = idempotencyStore.get(idempotencyKey);
            Reservation existing = dbClient.getReservation(existingId);
            return mapToCreateResponse(existing);
        }

        // 1. Validar disponibilidad con MS-Propiedades
        validateDates(request.checkIn(), request.checkOut());
        
        // 2. Construir objeto Reservation (status PENDING)
        Reservation reservation = buildPendingReservation(request, idempotencyKey);
        
        // 3. Persistir vía MS-ManejadorBD
        dbClient.saveReservation(reservation);
        
        // 4. Registrar auditoría
        AuditLog auditLog = createAuditLog(reservation.getId(), "CREATE_PENDING", "SYSTEM", "Reserva creada en estado PENDING");
        dbClient.saveAuditLog(auditLog);
        
        // 5. Publicar evento Kafka "reservation.created.pending"
        eventPublisher.publishReservationCreated(reservation, correlationId);
        
        // 6. Guardar relación idempotencyKey→reservation
        idempotencyStore.put(idempotencyKey, reservation.getId());
        
        // 7. Devolver DTO
        return mapToCreateResponse(reservation);
    }

    @Override
    public ConfirmReservationResponse confirmReservation(
            String reservationId,
            ConfirmReservationRequest request,
            String correlationId) {

        Reservation reservation = dbClient.getReservation(reservationId);

        if (Boolean.TRUE.equals(request.approved())) {
            // 1. Crear objeto Payment con status PAID
            Payment payment = buildPaidPayment(reservationId, request.transactionId());
            dbClient.savePayment(payment);
            
            // 2. Cambiar estado de la reserva a CONFIRMED
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation.setUpdatedAt(OffsetDateTime.now());
            dbClient.updateReservation(reservation);
            
            // 3. Audit + evento Kafka
            AuditLog auditLog = createAuditLog(reservationId, "CONFIRM", "SYSTEM", 
                "Reserva confirmada con transacción: " + request.transactionId());
            dbClient.saveAuditLog(auditLog);
            eventPublisher.publishReservationConfirmed(reservation, correlationId);
            
            return new ConfirmReservationResponse(
                    reservationId,
                    reservation.getStatus(),
                    PaymentStatus.PAID
            );
        } else {
            // pago rechazado
            reservation.setStatus(ReservationStatus.REJECTED);
            reservation.setUpdatedAt(OffsetDateTime.now());
            dbClient.updateReservation(reservation);
            
            AuditLog auditLog = createAuditLog(reservationId, "REJECT", "SYSTEM", 
                "Pago rechazado");
            dbClient.saveAuditLog(auditLog);
            eventPublisher.publishReservationRejected(reservation, correlationId);
            
            return new ConfirmReservationResponse(
                    reservationId,
                    reservation.getStatus(),
                    PaymentStatus.FAILED
            );
        }
    }

    @Override
    public ReservationResponse getReservation(String reservationId, String correlationId) {
        Reservation reservation = dbClient.getReservation(reservationId);
        List<Payment> payments = dbClient.getPaymentsByReservation(reservationId);
        return mapToReservationResponse(reservation, payments);
    }

    @Override
    public CancelReservationResponse cancelReservation(
            String reservationId,
            CancelReservationRequest request,
            String correlationId) {

        Reservation reservation = dbClient.getReservation(reservationId);

        // reglas de negocio: solo CONFIRMED / PENDING se pueden cancelar
        validateCancelable(reservation);

        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setUpdatedAt(OffsetDateTime.now());
        dbClient.updateReservation(reservation);

        String details = request != null ? 
            String.format("Cancelada por %s. Motivo: %s", request.origin(), request.reason()) :
            "Cancelada";
        AuditLog auditLog = createAuditLog(reservationId, "CANCEL", "SYSTEM", details);
        dbClient.saveAuditLog(auditLog);
        eventPublisher.publishReservationCancelled(reservation, correlationId);

        return new CancelReservationResponse(reservationId, reservation.getStatus());
    }
    
    // Métodos auxiliares privados
    
    private void validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn.isAfter(checkOut)) {
            throw new IllegalArgumentException("Check-in debe ser antes del check-out");
        }
        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in no puede ser en el pasado");
        }
    }
    
    private void validateCancelable(Reservation reservation) {
        if (reservation.getStatus() == ReservationStatus.CANCELLED ||
            reservation.getStatus() == ReservationStatus.CHECKED_OUT) {
            throw new IllegalStateException("La reserva no se puede cancelar en estado: " + reservation.getStatus());
        }
    }
    
    private Reservation buildPendingReservation(CreateReservationRequest request, String idempotencyKey) {
        Reservation reservation = new Reservation();
        reservation.setId(UUID.randomUUID().toString());
        reservation.setHotelId(request.hotelId());
        reservation.setRoomId(request.roomId());
        reservation.setClientId(request.clientId());
        reservation.setCheckIn(request.checkIn());
        reservation.setCheckOut(request.checkOut());
        reservation.setTotalPrice(request.totalPrice());
        reservation.setCurrency(request.currency());
        reservation.setStatus(ReservationStatus.PENDING);
        reservation.setIdempotencyKey(idempotencyKey);
        reservation.setCreatedBy("SYSTEM");
        reservation.setCreatedAt(OffsetDateTime.now());
        reservation.setUpdatedAt(OffsetDateTime.now());
        return reservation;
    }
    
    private Payment buildPaidPayment(String reservationId, String transactionId) {
        Payment payment = new Payment();
        payment.setId(UUID.randomUUID().toString());
        payment.setReservationId(reservationId);
        payment.setTransactionId(transactionId);
        payment.setStatus(PaymentStatus.PAID);
        payment.setProcessedAt(OffsetDateTime.now());
        return payment;
    }
    
    private AuditLog createAuditLog(String reservationId, String action, String performedBy, String details) {
        AuditLog log = new AuditLog();
        log.setId(UUID.randomUUID().toString());
        log.setReservationId(reservationId);
        log.setAction(action);
        log.setPerformedBy(performedBy);
        log.setPerformedAt(OffsetDateTime.now());
        log.setDetails(details);
        return log;
    }
    
    private CreateReservationResponse mapToCreateResponse(Reservation reservation) {
        return new CreateReservationResponse(
            reservation.getId(),
            reservation.getStatus(),
            reservation.getTotalPrice(),
            reservation.getCurrency()
        );
    }
    
    private ReservationResponse mapToReservationResponse(Reservation reservation, List<Payment> payments) {
        List<PaymentSummaryDto> paymentDtos = payments.stream()
            .map(p -> new PaymentSummaryDto(p.getTransactionId(), p.getStatus(), p.getAmount()))
            .collect(Collectors.toList());
            
        return new ReservationResponse(
            reservation.getId(),
            reservation.getHotelId(),
            reservation.getRoomId(),
            reservation.getClientId(),
            reservation.getCheckIn(),
            reservation.getCheckOut(),
            reservation.getTotalPrice(),
            reservation.getCurrency(),
            reservation.getStatus(),
            paymentDtos
        );
    }
}
