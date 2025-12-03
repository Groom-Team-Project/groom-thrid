package groom.backend.domain.auth.dto.response;

import groom.backend.domain.users.entity.Role;
import java.util.UUID;

public record UserInfoDto(
        UUID userId,
        String name,
        String email,
        String phone,
        Role role
) {
}
