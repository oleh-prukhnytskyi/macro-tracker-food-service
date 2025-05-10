package com.olehprukhnytskyi.macrotrackerfoodservice.exception;

import com.olehprukhnytskyi.macrotrackerfoodservice.dto.ApiError;
import com.olehprukhnytskyi.macrotrackerfoodservice.dto.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(
            MethodArgumentNotValidException ex) {
        String field = ex.getBindingResult().getFieldErrors().get(0).getField();
        String message = ex.getBindingResult().getFieldErrors().get(0).getDefaultMessage();
        ApiError error = new ApiError(field, message);
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGenericException(Exception ex) {
        ApiError error = new ApiError("server", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(error));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(NotFoundException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidationException(
            ConstraintViolationException ex) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + " " + v.getMessage())
                .findFirst()
                .orElse("Validation error");
        ApiError error = new ApiError("validation", message);
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<?>> handleMissingParams(
            MissingServletRequestParameterException ex) {
        String fieldName = ex.getParameterName();
        String message = "Missing required query parameter: " + fieldName;
        ApiError error = new ApiError("validation", message);
        return ResponseEntity.badRequest().body(ApiResponse.error(error));
    }
}
