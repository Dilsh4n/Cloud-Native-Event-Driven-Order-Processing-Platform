package com.orderplatform.payment_service.dto;

import java.time.Instant;

public record ErrorResponse (
        Instant timeStamp,
        int status,
        String error,
        String message,
        String path
){
}
