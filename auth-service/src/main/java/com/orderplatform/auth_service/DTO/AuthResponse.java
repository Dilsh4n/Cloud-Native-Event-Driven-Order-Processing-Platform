package com.orderplatform.auth_service.DTO;

public record AuthResponse(
        String token,
        long expiresIn) {
}
