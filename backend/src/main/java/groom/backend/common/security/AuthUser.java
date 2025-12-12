package groom.backend.common.security;

import groom.backend.domain.users.entity.Role;
import java.util.UUID;

public record AuthUser(
        UUID userId,
        Role role,
        Long relationId
) {
}
