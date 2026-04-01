package com.videostreaming.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiError {

    private int statusCode;
    private String statusMessage;
    private List<ApiErrorDetail> errors;
    private LocalDateTime timestamp;

    public ApiError() {
    }

    public ApiError(int statusCode, String statusMessage) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.timestamp = LocalDateTime.now();
    }

    public ApiError(int statusCode, String statusMessage, List<ApiErrorDetail> errors) {
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
        this.errors = errors;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public List<ApiErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(List<ApiErrorDetail> errors) {
        this.errors = errors;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}