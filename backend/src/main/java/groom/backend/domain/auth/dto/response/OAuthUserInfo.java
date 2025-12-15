package groom.backend.domain.auth.dto.response;

import groom.backend.domain.users.entity.Provider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * OAuth Provider로부터 받는 사용자 정보
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuthUserInfo {

    private Provider provider;
    private String providerId;  // OAuth provider의 사용자 고유 ID
    private String email;
    private String name;
}
