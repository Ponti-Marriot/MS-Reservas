package com.pontimarriot.reservas.api.dto;

import com.pontimarriot.reservas.domain.enums.ReservationStatus;

public record CancelReservationResponse(
        String reservationId,
        ReservationStatus status
) {
}
