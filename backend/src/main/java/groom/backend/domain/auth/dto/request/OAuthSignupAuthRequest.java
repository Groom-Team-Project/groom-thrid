package groom.backend.domain.auth.dto.request;

public record OAuthSignupAuthRequest(
        String provider,
        String providerToken
) {

}
