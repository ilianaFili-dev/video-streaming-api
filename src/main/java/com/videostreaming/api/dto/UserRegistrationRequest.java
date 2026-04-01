package com.videostreaming.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class UserRegistrationRequest {

    @NotBlank(message = "Username is required")
    @Pattern(
            regexp = "^[a-zA-Z0-9]+$",
            message = "Username must be alphanumeric (no spaces or special characters)"
    )
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
            message = "Password must be at least 8 characters with at least 1 uppercase letter and 1 number"
    )
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotNull(message = "Date of birth is required")
    @JsonFormat(
            shape = JsonFormat.Shape.STRING,
            pattern = "yyyy-MM-dd",
            timezone = "UTC"
    )
    private LocalDate dateOfBirth;

    // Optional — if provided, must be exactly 16 digits and pass Luhn check
    @Pattern(
            regexp = "^\\d{16}$",
            message = "Credit card number must be exactly 16 digits",
            groups = CreditCardValidation.class
    )
    private String creditCardNumber;

    public UserRegistrationRequest() {
    }

    public UserRegistrationRequest(String username, String password, String email,
                                   LocalDate dateOfBirth, String creditCardNumber) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.creditCardNumber = creditCardNumber;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getCreditCardNumber() {
        return creditCardNumber;
    }

    public void setCreditCardNumber(String creditCardNumber) {
        this.creditCardNumber = creditCardNumber;
    }

    public boolean hasCreditCard() {
        return creditCardNumber != null && !creditCardNumber.isBlank();
    }
}