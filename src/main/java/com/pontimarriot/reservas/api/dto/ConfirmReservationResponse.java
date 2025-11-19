package com.pontimarriot.reservas.api.dto;

import com.pontimarriot.reservas.domain.enums.PaymentStatus;
import com.pontimarriot.reservas.domain.enums.ReservationStatus;

public record ConfirmReservationResponse(
        String reservationId,
        ReservationStatus status,
        PaymentStatus paymentStatus
) {}