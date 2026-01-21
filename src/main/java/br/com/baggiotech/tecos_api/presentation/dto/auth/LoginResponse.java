package br.com.baggiotech.tecos_api.presentation.dto.auth;

import br.com.baggiotech.tecos_api.presentation.dto.user.UserResponse;

public record LoginResponse(
        UserResponse user,
        String token,
        String tokenType
) {
}
