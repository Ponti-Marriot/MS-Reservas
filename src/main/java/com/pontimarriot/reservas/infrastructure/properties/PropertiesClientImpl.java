package com.pontimarriot.reservas.infrastructure.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;

@Component
public class PropertiesClientImpl implements PropertiesClient {

    private final WebClient webClient;

    public PropertiesClientImpl(
            WebClient.Builder builder,
            @Value("${properties.client.base-url}") String baseUrl) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public boolean isRoomAvailable(String roomId, LocalDate checkIn, LocalDate checkOut, int numRooms) {
        try {
            Boolean result = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/rooms/{roomId}/availability")
                            .queryParam("checkIn", checkIn)
                            .queryParam("checkOut", checkOut)
                            .queryParam("numRooms", numRooms)
                            .build(roomId))
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            // Log error y devolver false por seguridad
            return false;
        }
    }
}
