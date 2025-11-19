package com.pontimarriot.reservas.api.dto;

import com.pontimarriot.reservas.domain.enums.PaymentStatus;
import com.pontimarriot.reservas.domain.enums.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReservationResponse(
        String reservationId,
        String hotelId,
        String roomId,
        String clientId,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalPrice,
        String currency,
        ReservationStatus status,
        List<PaymentSummaryDto> payments
) {}

record PaymentSummaryDto(
        String transactionId,
        PaymentStatus status,
        BigDecimal amount
) {}

record CancelReservationRequest(
        String reason,
        String origin,   // CLIENTE | HOTEL | SYSTEM
        BigDecimal refundAmount
) {}

record CancelReservationResponse(
        String reservationId,
        ReservationStatus status
) {}