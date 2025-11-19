package com.pontimarriot.reservas.domain.model;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import com.pontimarriot.reservas.domain.enums.PaymentStatus;

public class Payment {
    private String id;
    private String reservationId;
    private String transactionId;      // <BANCO>-YYYYMMDD-XXXX
    private PaymentStatus status;
    private BigDecimal amount;
    private OffsetDateTime processedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReservationId() {
        return reservationId;
    }

    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }

    public void setProcessedAt(OffsetDateTime processedAt) {
        this.processedAt = processedAt;
    }
}