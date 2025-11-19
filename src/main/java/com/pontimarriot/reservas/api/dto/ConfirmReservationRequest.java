package com.pontimarriot.reservas.api.dto;

public record ConfirmReservationRequest(
        String transactionId,      // viene del Banco vía Turismo
        Boolean approved           // true = CONFIRMADO, false = DENEGADO
) {}