package com.waregang.receiving_service.user.api.dto;

public record AuthenticationResponse(
        String accessToken,
        String refreshToken
) {
}
