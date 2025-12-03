package groom.backend.domain.auth.dto.response;

public record CommonAuthResponse(
        String accessToken,
        String refreshToken
) {
}
