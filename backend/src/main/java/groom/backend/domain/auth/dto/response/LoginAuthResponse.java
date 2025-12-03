package groom.backend.domain.auth.dto.response;

public record LoginAuthResponse(
        String accessToken,
        String refreshToken
) {
}
