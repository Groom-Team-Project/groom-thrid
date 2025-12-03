package groom.backend.domain.auth.dto.request;

import groom.backend.domain.users.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FormSignupAuthRequest(
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 100)
        String name,

        @Email(message = "올바른 이메일 형식이 아닙니다")
        @NotBlank(message = "이메일은 필수입니다")
        String email,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 20)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$",
                message = "비밀번호는 대소문자, 숫자, 특수문자를 포함해야 합니다")
        String password,

        String phone,

        Role role
) {
    // compact constructor: role 기본값 처리
    public FormSignupAuthRequest {
        if (role == null) {
            role = Role.USER;  // 내부 로컬 변수 덮어쓰기 → 필드에도 반영됨
        }
    }
}
