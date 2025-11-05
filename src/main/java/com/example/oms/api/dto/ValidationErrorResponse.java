package com.example.oms.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class ValidationErrorResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private final Instant timestamp;
    private final int status;
    private final String error;
    private final String message;
    private final String path;
    private final Map<String, String> validationErrors;

    public ValidationErrorResponse(Instant timestamp, int status, String error, 
                                 String message, String path) {
        this.timestamp = timestamp;
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
        this.validationErrors = new HashMap<>();
    }

    public static ValidationErrorResponse of(HttpStatus status, String message, String path) {
        return new ValidationErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                path
        );
    }

    public void addValidationError(String field, String message) {
        this.validationErrors.put(field, message);
    }

    public void addValidationErrors(Map<String, String> errors) {
        this.validationErrors.putAll(errors);
    }

    // Getters
    public Instant getTimestamp() {
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

    public String getPath() {
        return path;
    }

    public Map<String, String> getErrors() {
        return validationErrors;
    }
}

