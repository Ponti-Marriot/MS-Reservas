package com.pontimarriot.reservas.api.dto;

import java.math.BigDecimal;

public record CancelReservationRequest(
        String reason,
        String origin,   // CLIENTE | HOTEL | SYSTEM
        BigDecimal refundAmount
) {
}
