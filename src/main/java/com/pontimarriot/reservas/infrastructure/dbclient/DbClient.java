package com.pontimarriot.reservas.infrastructure.dbclient;

import com.pontimarriot.reservas.domain.model.AuditLog;
import com.pontimarriot.reservas.domain.model.Payment;
import com.pontimarriot.reservas.domain.model.Reservation;
import java.util.List;

public interface DbClient {
    void saveReservation(Reservation reservation);
    void updateReservation(Reservation reservation);
    Reservation getReservation(String reservationId);

    void savePayment(Payment payment);
    List<Payment> getPaymentsByReservation(String reservationId);

    void saveAuditLog(AuditLog log);
}
