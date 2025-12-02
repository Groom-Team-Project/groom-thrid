package groom.backend.domain.users.dto.response;

import groom.backend.domain.users.entity.Role;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.Builder;

@Builder
public record UserResponse(
        UUID userId,
        String name,
        String email,
        String phone,
        Role role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
