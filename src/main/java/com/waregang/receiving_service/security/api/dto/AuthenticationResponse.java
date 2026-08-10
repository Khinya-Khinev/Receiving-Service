package com.waregang.receiving_service.security.api.dto;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken
) {
}
