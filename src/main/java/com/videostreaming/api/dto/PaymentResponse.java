package com.videostreaming.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.videostreaming.api.model.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {

    private Long id;
    private String username;
    private BigDecimal amount;

    @JsonProperty("paymentStatus")
    private PaymentStatus paymentStatus;

    @JsonProperty("creditCardLastFour")
    private String creditCardLastFour;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    public PaymentResponse() {
    }

    public PaymentResponse(Long id, String username, BigDecimal amount,
                           PaymentStatus paymentStatus, String creditCardLastFour,
                           LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.amount = amount;
        this.paymentStatus = paymentStatus;
        this.creditCardLastFour = creditCardLastFour;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public String getCreditCardLastFour() {
        return creditCardLastFour;
    }

    public void setCreditCardLastFour(String creditCardLastFour) {
        this.creditCardLastFour = creditCardLastFour;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}