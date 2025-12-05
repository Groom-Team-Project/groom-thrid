package groom.backend.domain.users.dto.request;

import groom.backend.domain.users.entity.Role;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(

        String name,
        
        @Size(max = 15)
        String phone,

        Role role
) {
}
