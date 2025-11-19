package com.pontimarriot.reservas.infrastructure.properties;

import java.time.LocalDate;

public interface PropertiesClient {
    boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut, int numRooms);
}
