package com.pontimarriot.reservas.api.dto;

import com.pontimarriot.reservas.domain.enums.PaymentStatus;
import java.math.BigDecimal;

public record PaymentSummaryDto(
        String transactionId,
        PaymentStatus status,
        BigDecimal amount
) {
}
