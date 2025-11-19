package com.pontimarriot.reservas.infrastructure.kafka;

import com.pontimarriot.reservas.domain.model.Reservation;

public interface ReservationEventPublisher {
    void publishReservationCreated(Reservation reservation, String correlationId);
    void publishReservationConfirmed(Reservation reservation, String correlationId);
    void publishReservationCancelled(Reservation reservation, String correlationId);
    void publishReservationRejected(Reservation reservation, String correlationId);
}
