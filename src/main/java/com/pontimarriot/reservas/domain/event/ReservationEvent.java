package com.pontimarriot.reservas.domain.event;

import java.time.OffsetDateTime;

public record ReservationEvent(
        String eventType,          // CREATED_PENDING, CONFIRMED, CANCELLED, REJECTED...
        String reservationId,
        String correlationId,
        OffsetDateTime occurredAt
) {}
