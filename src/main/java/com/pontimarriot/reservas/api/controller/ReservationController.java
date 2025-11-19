package com.pontimarriot.reservas.api.controller;

import com.pontimarriot.reservas.api.dto.*;
import com.pontimarriot.reservas.application.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/availability/search")
    public AvailabilitySearchResponse searchAvailability(
            @RequestBody @Valid AvailabilitySearchRequest request,
            @RequestHeader("X-Correlation-Id") String correlationId) {
        return reservationService.searchAvailability(request, correlationId);
    }

    @PostMapping("/reservations")
    public ResponseEntity<CreateReservationResponse> createReservation(
            @RequestBody @Valid CreateReservationRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-Correlation-Id") String correlationId) {

        CreateReservationResponse response =
                reservationService.createReservation(request, idempotencyKey, correlationId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/reservations/{id}/confirm")
    public ConfirmReservationResponse confirmReservation(
            @PathVariable String id,
            @RequestBody @Valid ConfirmReservationRequest request,
            @RequestHeader("X-Correlation-Id") String correlationId) {
        return reservationService.confirmReservation(id, request, correlationId);
    }

    @GetMapping("/reservations/{id}")
    public ReservationResponse getReservation(
            @PathVariable String id,
            @RequestHeader("X-Correlation-Id") String correlationId) {
        return reservationService.getReservation(id, correlationId);
    }

    @DeleteMapping("/reservations/{id}")
    public CancelReservationResponse cancelReservation(
            @PathVariable String id,
            @RequestBody(required = false) CancelReservationRequest request,
            @RequestHeader("X-Correlation-Id") String correlationId) {
        return reservationService.cancelReservation(id, request, correlationId);
    }
}
