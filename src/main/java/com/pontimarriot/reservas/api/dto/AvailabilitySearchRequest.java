package com.pontimarriot.reservas.api.dto;

import java.time.LocalDate;

public record AvailabilitySearchRequest(
        String hotelId,
        String roomTypeCode,
        LocalDate checkIn,
        LocalDate checkOut,
        int numRooms,
        int numAdults
) {}