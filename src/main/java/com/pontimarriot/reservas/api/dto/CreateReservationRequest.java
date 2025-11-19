package com.pontimarriot.reservas.api.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateReservationRequest(
        String hotelId,
        String roomId,
        String clientId,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalPrice,
        String currency
) {}