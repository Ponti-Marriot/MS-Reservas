package com.pontimarriot.reservas.api.dto;

import com.pontimarriot.reservas.domain.enums.ReservationStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ReservationResponse(
        String reservationId,
        String hotelId,
        String roomId,
        String clientId,
        LocalDate checkIn,
        LocalDate checkOut,
        BigDecimal totalPrice,
        String currency,
        ReservationStatus status,
        List<PaymentSummaryDto> payments
) {}
