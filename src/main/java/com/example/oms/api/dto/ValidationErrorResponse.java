package com.example.oms.api.dto;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

public class ValidationErrorResponse {
    private final String timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final Map<String, String> errors;

    public ValidationErrorResponse(String timestamp, int status, String error, String message, Map<String, String> errors) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.errors = errors;
    }

    public static ValidationErrorResponse of(HttpStatus status, String message, Map<String, String> errors) {
        return new ValidationErrorResponse(
                Instant.now().toString(),
                status.value(),
                status.getReasonPhrase(),
                message,
                errors
        );
    }

    public String getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getErrors() {
        return errors;
    }
}

