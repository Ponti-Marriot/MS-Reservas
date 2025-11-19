package com.pontimarriot.reservas.api.dto;

import java.math.BigDecimal;
import java.util.List;

public record AvailabilitySearchResponse(
        String searchId,
        List<AvailableRoomDto> availableRooms
) {}

record AvailableRoomDto(
        String roomId,
        String roomTypeCode,
        BigDecimal pricePerNight,
        BigDecimal totalPrice,
        String currency
) {}