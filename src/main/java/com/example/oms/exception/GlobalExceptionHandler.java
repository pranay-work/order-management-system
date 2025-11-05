package com.example.oms.exception;

import com.example.oms.api.dto.ErrorResponse;
import com.example.oms.api.dto.ValidationErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        
        String path = getRequestPath(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND, 
            ex.getMessage(),
            path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, WebRequest request) {
        
        String path = getRequestPath(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            ex.getMessage(),
            path
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, WebRequest request) {
        
        String path = getRequestPath(request);
        ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            path
        );
        
        ex.getConstraintViolations().forEach(violation -> 
            errorResponse.addValidationError(
                getFieldName(violation.getPropertyPath().toString()),
                violation.getMessage()
            )
        );
        
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatus status,
            WebRequest request) {
        
        String path = getRequestPath(request);
        ValidationErrorResponse errorResponse = ValidationErrorResponse.of(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            path
        );

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = error instanceof FieldError 
                ? ((FieldError) error).getField() 
                : error.getObjectName();
            errorResponse.addValidationError(fieldName, error.getDefaultMessage());
        });

        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(
            Exception ex, WebRequest request) {
        
        String path = getRequestPath(request);
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "An unexpected error occurred",
            path
        );
        
        // In production, consider using a proper logger
        ex.printStackTrace();
        
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            return ((ServletWebRequest) request).getRequest().getRequestURI();
        }
        return "";
    }
    
    private String getFieldName(String propertyPath) {
        // Convert from method parameter name to field name if needed
        // e.g., "createOrder.createOrderRequest.items[0].productId" -> "items[0].productId"
        String[] parts = propertyPath.split("\\.");
        if (parts.length > 0) {
            return parts[parts.length - 1];
        }
        return propertyPath;
    }
}
