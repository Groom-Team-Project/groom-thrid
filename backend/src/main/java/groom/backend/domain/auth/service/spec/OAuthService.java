package groom.backend.domain.auth.service.spec;

import groom.backend.domain.auth.dto.response.CommonAuthResponse;
import groom.backend.domain.auth.dto.response.OAuthUserInfo;
import groom.backend.domain.users.entity.Provider;

public interface OAuthService {

    /**
     * OAuth provider로부터 Access Token 발급
     *
     * @param provider OAuth provider (Naver, Google, Kakao)
     * @param code     Authorization code
     * @return Access Token
     */
    String getAccessToken(Provider provider, String code);

    /**
     * OAuth provider로부터 사용자 정보 조회
     *
     * @param provider    OAuth provider (Naver, Google, Kakao)
     * @param accessToken Access Token
     * @return 사용자 정보 (providerId, email, name)
     */
    OAuthUserInfo getUserInfo(Provider provider, String accessToken);

    /**
     * OAuth 로그인/회원가입 처리
     * - 사용자가 존재하면 로그인
     * - 사용자가 없으면 회원가입 페이지로 리다이렉트 위한 정보 반환
     *
     * @param provider OAuth provider
     * @param code     Authorization code
     * @return JWT 토큰 또는 회원가입 정보
     */
    CommonAuthResponse processOAuthCallback(Provider provider, String code);
}
