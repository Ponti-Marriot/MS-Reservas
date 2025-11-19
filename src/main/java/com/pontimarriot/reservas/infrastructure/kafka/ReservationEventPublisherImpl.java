package com.pontimarriot.reservas.infrastructure.kafka;

import com.pontimarriot.reservas.domain.event.ReservationEvent;
import com.pontimarriot.reservas.domain.model.Reservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class ReservationEventPublisherImpl implements ReservationEventPublisher {

    private final KafkaTemplate<String, ReservationEvent> kafkaTemplate;
    private final String topicName;

    public ReservationEventPublisherImpl(
            KafkaTemplate<String, ReservationEvent> kafkaTemplate,
            @Value("${kafka.topics.reservation-events}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    @Override
    public void publishReservationCreated(Reservation reservation, String correlationId) {
        ReservationEvent event = new ReservationEvent(
                "CREATED_PENDING",
                reservation.getId(),
                correlationId,
                OffsetDateTime.now()
        );
        kafkaTemplate.send(topicName, reservation.getId(), event);
    }

    @Override
    public void publishReservationConfirmed(Reservation reservation, String correlationId) {
        ReservationEvent event = new ReservationEvent(
                "CONFIRMED",
                reservation.getId(),
                correlationId,
                OffsetDateTime.now()
        );
        kafkaTemplate.send(topicName, reservation.getId(), event);
    }

    @Override
    public void publishReservationCancelled(Reservation reservation, String correlationId) {
        ReservationEvent event = new ReservationEvent(
                "CANCELLED",
                reservation.getId(),
                correlationId,
                OffsetDateTime.now()
        );
        kafkaTemplate.send(topicName, reservation.getId(), event);
    }

    @Override
    public void publishReservationRejected(Reservation reservation, String correlationId) {
        ReservationEvent event = new ReservationEvent(
                "REJECTED",
                reservation.getId(),
                correlationId,
                OffsetDateTime.now()
        );
        kafkaTemplate.send(topicName, reservation.getId(), event);
    }
}
