package groom.backend.domain.users.dto.request;

import groom.backend.domain.users.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
        String name,

        @Email(message = "올바른 이메일 형식이 아닙니다")
        @NotBlank(message = "이메일은 필수입니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하이어야 합니다")
        String password,

        String phone,

        Role role
) {
    // compact constructor: role 기본값 처리
    public CreateUserRequest {
        if (role == null) {
            role = Role.USER;
        }
    }
}
