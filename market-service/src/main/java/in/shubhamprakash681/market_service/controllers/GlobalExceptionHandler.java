package in.shubhamprakash681.market_service.controllers;

import in.shubhamprakash681.common_lib.api.ApiError;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ApiError> responseStatus(ResponseStatusException exception) {
        int status = exception.getStatusCode().value();
        String error = exception.getStatusCode().toString();
        return ResponseEntity.status(status).body(ApiError.of(status, error, exception.getReason()));
    }
}
