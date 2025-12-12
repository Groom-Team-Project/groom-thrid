package groom.backend.domain.auth.dto.request;

import groom.backend.domain.users.entity.Provider;
import groom.backend.domain.users.entity.Role;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record OAuthSignupAuthRequest(
        @NotBlank(message = "이름은 필수입니다")
        @Size(max = 100)
        String name,

        @Email(message = "올바른 이메일 형식이 아닙니다")
        @NotBlank(message = "이메일은 필수입니다")
        String email,

        String phone,

        @NotBlank
        @Enumerated
        Role role,

        @NotBlank
        Provider provider,

        @NotBlank
        String providerToken
) {

}
