package com.pontimarriot.reservas.api.dto;

import com.pontimarriot.reservas.domain.enums.ReservationStatus;
import java.math.BigDecimal;

public record CreateReservationResponse(
        String reservationId,
        ReservationStatus status,
        BigDecimal totalPrice,
        String currency
) {}