package com.pontimarriot.reservas.application.service;

import com.pontimarriot.reservas.api.dto.*;

public interface ReservationService {

    AvailabilitySearchResponse searchAvailability(
            AvailabilitySearchRequest request, String correlationId);

    CreateReservationResponse createReservation(
            CreateReservationRequest request,
            String idempotencyKey,
            String correlationId);

    ConfirmReservationResponse confirmReservation(
            String reservationId,
            ConfirmReservationRequest request,
            String correlationId);

    ReservationResponse getReservation(String reservationId, String correlationId);

    CancelReservationResponse cancelReservation(
            String reservationId,
            CancelReservationRequest request,
            String correlationId);
}
