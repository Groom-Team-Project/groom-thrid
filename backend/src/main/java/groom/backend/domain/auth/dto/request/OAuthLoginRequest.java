package groom.backend.domain.auth.dto.request;

import groom.backend.domain.users.entity.Provider;

public record OAuthLoginRequest(
        Provider provider,
        String providerId
) {
}
