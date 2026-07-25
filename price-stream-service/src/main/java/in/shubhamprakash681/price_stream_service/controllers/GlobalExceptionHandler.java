package in.shubhamprakash681.price_stream_service.controllers;

import in.shubhamprakash681.common_lib.api.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> validationError(MethodArgumentNotValidException exception) {
        var details = exception.getBindingResult().getFieldErrors().stream().map(this::format).toList();

        return ResponseEntity.badRequest().body(ApiError.of(400, "Bad Request", "Validation failed", details));
    }

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> responseStatusError(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        String error = exception.getStatusCode().toString();

        return ResponseEntity.status(status).body(ApiError.of(status, error, exception.getReason()));
    }

    private String format(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
