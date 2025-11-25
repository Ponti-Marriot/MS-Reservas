package com.pontimarriot.reservas.infrastructure.kafka;

import com.pontimarriot.reservas.domain.event.ReservationEvent;
import com.pontimarriot.reservas.domain.model.Reservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
public class ReservationEventPublisherImpl implements ReservationEventPublisher {

    @Value("${spring.cloud.stream.bindings.reservationCreated-out-0.destination}")
    private String topicCreated;

    @Value("${spring.cloud.stream.bindings.reservationConfirmed-out-0.destination}")
    private String topicConfirmed;

    @Value("${spring.cloud.stream.bindings.reservationCancelled-out-0.destination}")
    private String topicCancelled;

    @Value("${spring.cloud.stream.bindings.reservationRejected-out-0.destination}")
    private String topicRejected;

    private final StreamBridge streamBridge;

    public ReservationEventPublisherImpl(StreamBridge streamBridge) {
        this.streamBridge = streamBridge;
    }

    @Override
    public void publishReservationCreated(Reservation reservation, String correlationId) {
        ReservationEvent event = new ReservationEvent(
                "CREATED_PENDING",
                reservation.getId(),
                correlationId,
                OffsetDateTime.now()
        );

        streamBridge.send(
                topicCreated,
                MessageBuilder
                        .withPayload(event)
                        .setHeader("eventType", "CREATED_PENDING")
                        .build()
        );
    }

    @Override
    public void publishReservationConfirmed(Reservation reservation, String correlationId) {
        ReservationEvent event = new ReservationEvent(
                "CONFIRMED",
                reservation.getId(),
                correlationId,
                OffsetDateTime.now()
        );

        streamBridge.send(
                topicConfirmed,
                MessageBuilder
                        .withPayload(event)
                        .setHeader("eventType", "CONFIRMED")
                        .build()
        );
    }

    @Override
    public void publishReservationCancelled(Reservation reservation, String correlationId) {
        ReservationEvent event = new ReservationEvent(
                "CANCELLED",
                reservation.getId(),
                correlationId,
                OffsetDateTime.now()
        );

        streamBridge.send(
                topicCancelled,
                MessageBuilder
                        .withPayload(event)
                        .setHeader("eventType", "CANCELLED")
                        .build()
        );
    }

    @Override
    public void publishReservationRejected(Reservation reservation, String correlationId) {
        ReservationEvent event = new ReservationEvent(
                "REJECTED",
                reservation.getId(),
                correlationId,
                OffsetDateTime.now()
        );

        streamBridge.send(
                topicRejected,
                MessageBuilder
                        .withPayload(event)
                        .setHeader("eventType", "REJECTED")
                        .build()
        );
    }
}
