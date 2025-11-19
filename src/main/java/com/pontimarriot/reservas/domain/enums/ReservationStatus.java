package com.pontimarriot.reservas.domain.enums;

public enum ReservationStatus {
    PENDING,      // creada, esperando pago
    CONFIRMED,    // pago aprobado
    CHECKED_IN,
    CHECKED_OUT,
    CANCELLED,
    REJECTED      // por pago fallido o política
}