package com.pontimarriot.reservas.infrastructure.dbclient;

import com.pontimarriot.reservas.domain.model.AuditLog;
import com.pontimarriot.reservas.domain.model.Payment;
import com.pontimarriot.reservas.domain.model.Reservation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class DbClientRest implements DbClient {

    private final WebClient webClient;

    public DbClientRest(WebClient.Builder builder,
                        @Value("${dbclient.base-url}") String baseUrl) {
        this.webClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public void saveReservation(Reservation reservation) {
        webClient.post()
                .uri("/internal/reservations")
                .bodyValue(reservation)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Override
    public void updateReservation(Reservation reservation) {
        webClient.put()
                .uri("/internal/reservations/{id}", reservation.getId())
                .bodyValue(reservation)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Override
    public Reservation getReservation(String reservationId) {
        return webClient.get()
                .uri("/internal/reservations/{id}", reservationId)
                .retrieve()
                .bodyToMono(Reservation.class)
                .block();
    }

    @Override
    public void savePayment(Payment payment) {
        webClient.post()
                .uri("/internal/payments")
                .bodyValue(payment)
                .retrieve()
                .toBodilessEntity()
                .block();
    }

    @Override
    public List<Payment> getPaymentsByReservation(String reservationId) {
        return webClient.get()
                .uri("/internal/payments/reservation/{id}", reservationId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<Payment>>() {})
                .block();
    }

    @Override
    public void saveAuditLog(AuditLog log) {
        webClient.post()
                .uri("/internal/audit-logs")
                .bodyValue(log)
                .retrieve()
                .toBodilessEntity()
                .block();
    }
}
