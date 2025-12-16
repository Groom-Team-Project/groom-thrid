package groom.backend.domain.auth.dto.request;

import groom.backend.domain.users.entity.Provider;
import groom.backend.domain.users.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OAuthSignupAuthRequest(
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 100)
        String name,

        @Email(message = "올바른 이메일 형식이 아닙니다")
        String email, // OAuth에서 이메일을 받지 못할 수 있으므로 필수 아님 (사용자 직접 입력)

        String phone,

        @NotNull(message = "역할은 필수입니다")
        Role role,

        @NotNull(message = "OAuth 제공자 정보는 필수입니다")
        Provider provider,

        @NotBlank(message = "Provider ID는 필수입니다")
        String providerId
) {

}
