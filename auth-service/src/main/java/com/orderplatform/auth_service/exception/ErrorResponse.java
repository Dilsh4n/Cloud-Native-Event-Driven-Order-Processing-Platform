package com.orderplatform.auth_service.exception;

import lombok.Builder;

import java.time.Instant;
import java.util.List;

@Builder
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        List<ValidationErrors> validationErrors
) {
    public record ValidationErrors(
            String field,
            String message
    ) {
    }
}
