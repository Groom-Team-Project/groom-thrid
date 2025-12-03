package groom.backend.domain.auth.dto.request;

public record OAuthLoginRequest(
        String provider,
        String providerToken
) {
}
