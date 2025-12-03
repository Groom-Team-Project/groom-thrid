package groom.backend.domain.auth.dto.request;

public record RefreshAuthRequest(
        String refreshToken
) {
}
