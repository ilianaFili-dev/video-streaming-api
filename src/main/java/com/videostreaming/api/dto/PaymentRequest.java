package com.videostreaming.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

public class PaymentRequest {

    @NotBlank(message = "Credit card number is required")
    @Pattern(
            regexp = "^\\d{16}$",
            message = "Credit card number must be exactly 16 digits"
    )
    private String creditCardNumber;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.00", message = "Amount must be at least 1.00")
    @DecimalMax(value = "999.99", message = "Amount must be at most 999.99")
    @Digits(integer = 3, fraction = 2, message = "Amount must have at most 3 digits and 2 decimal places")
    private BigDecimal amount;

    public PaymentRequest() {
    }

    public PaymentRequest(String creditCardNumber, BigDecimal amount) {
        this.creditCardNumber = creditCardNumber;
        this.amount = amount;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}