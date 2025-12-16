package groom.backend.domain.auth.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import groom.backend.common.config.OAuthConfig;
import groom.backend.common.exception.BusinessException;
import groom.backend.common.exception.ErrorCode;
import groom.backend.common.security.JwtUtil;
import groom.backend.domain.auth.dto.response.CommonAuthResponse;
import groom.backend.domain.auth.dto.response.OAuthTokenResponse;
import groom.backend.domain.auth.dto.response.OAuthUserInfo;
import groom.backend.domain.auth.repository.spec.RefreshTokenRedisRepository;
import groom.backend.domain.auth.service.spec.OAuthService;
import groom.backend.domain.auth.vo.RefreshTokenValue;
import groom.backend.domain.users.entity.Provider;
import groom.backend.domain.users.entity.User;
import groom.backend.domain.users.entity.UserCredential;
import groom.backend.domain.users.entity.UserRelation;
import groom.backend.domain.users.repository.spec.UserCredentialRepository;
import groom.backend.domain.users.repository.spec.UserRelationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OAuthServiceImpl implements OAuthService {

    private final OAuthConfig oauthConfig;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserCredentialRepository userCredentialRepository;
    private final JwtUtil jwtUtil;
    private final RefreshTokenRedisRepository refreshTokenRedisRepository;
    private final UserRelationRepository userRelationRepository;

    @Override
    public String getAccessToken(Provider provider, String code) {
        OAuthConfig.ProviderConfig config = oauthConfig.getProviderConfig(provider.name());

        // Access Token 발급 요청
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", config.getClientId());
        params.add("client_secret", config.getClientSecret());
        params.add("code", code);
        params.add("redirect_uri", config.getRedirectUri());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<OAuthTokenResponse> response = restTemplate.postForEntity(
                    config.getTokenUrl(),
                    request,
                    OAuthTokenResponse.class
            );

            if (response.getBody() == null || response.getBody().getAccessToken() == null) {
                throw new BusinessException(ErrorCode.OAUTH_ACCESS_TOKEN_ERROR);
            }

            return response.getBody().getAccessToken();
        } catch (Exception e) {
            log.error("OAuth Access Token 발급 실패 - provider: {}, error: {}", provider, e.getMessage());
            throw new BusinessException(ErrorCode.OAUTH_ACCESS_TOKEN_ERROR);
        }
    }

    @Override
    public OAuthUserInfo getUserInfo(Provider provider, String accessToken) {
        OAuthConfig.ProviderConfig config = oauthConfig.getProviderConfig(provider.name());

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    config.getUserInfoUrl(),
                    HttpMethod.GET,
                    request,
                    String.class
            );

            String responseBody = response.getBody();
            if (responseBody == null) {
                throw new BusinessException(ErrorCode.OAUTH_USER_INFO_ERROR);
            }

            // 응답 로그 출력 (디버깅용)
            log.info("OAuth 사용자 정보 응답 - provider: {}, response: {}", provider, responseBody);

            // Provider별로 응답 형식이 다르므로 파싱 처리
            return parseUserInfo(provider, responseBody);
        } catch (Exception e) {
            log.error("OAuth 사용자 정보 조회 실패 - provider: {}, error: {}", provider, e.getMessage());
            throw new BusinessException(ErrorCode.OAUTH_USER_INFO_ERROR);
        }
    }

    @Override
    public CommonAuthResponse processOAuthLogin(OAuthUserInfo userInfo) {
        // 1. DB에서 사용자 조회 (providerId + provider로 조회)
        Optional<UserCredential> credentialOpt = userCredentialRepository
                .findByProviderIdAndProvider(userInfo.getProviderId(), userInfo.getProvider());

        if (credentialOpt.isEmpty()) {
            // 사용자가 없음 - 회원가입 필요
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 4. 로그인 처리
        UserCredential credential = credentialOpt.get();
        User user = credential.getUser();

        // 계정 활성화 여부 확인
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.DEACTIVATED_USER);
        }

        // 5. JWT 토큰 생성 (userId + role + name + relationId 포함)
        Long relationId = switch (user.getRole()) {
            case USER -> userRelationRepository.findByUserId(user.getId())
                    .map(UserRelation::getId)
                    .orElse(null);
            case GUARDIAN -> userRelationRepository.findByGuardianId(user.getId())
                    .map(UserRelation::getId)
                    .orElse(null);
            default -> null;
        };

        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole(), user.getName(), relationId);
        String refreshTokenString = jwtUtil.generateRefreshToken(user.getId());

        // 6. RefreshToken Redis에 저장 (TTL 자동 적용)
        RefreshTokenValue refreshTokenValue = RefreshTokenValue.of(user.getId());
        refreshTokenRedisRepository.save(refreshTokenString, refreshTokenValue);

        // 7. 토큰 반환
        return new CommonAuthResponse(newAccessToken, refreshTokenString);
    }

    @Override
    @Deprecated
    public CommonAuthResponse processOAuthCallback(Provider provider, String code) {
        // 1. Access Token 발급
        String accessToken = getAccessToken(provider, code);

        // 2. 사용자 정보 조회
        OAuthUserInfo userInfo = getUserInfo(provider, accessToken);

        // 3. 로그인 처리
        return processOAuthLogin(userInfo);
    }

    /**
     * Provider별로 사용자 정보 응답 파싱
     */
    private OAuthUserInfo parseUserInfo(Provider provider, String responseBody) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);

            return switch (provider) {
                case Naver -> {
                    JsonNode response = rootNode.get("response");
                    yield OAuthUserInfo.builder()
                            .provider(Provider.Naver)
                            .providerId(response.get("id").asText())
                            .email(response.has("email") ? response.get("email").asText() : null)
                            .name(response.has("name") ? response.get("name").asText() : null)
                            .build();
                }
                case Google -> OAuthUserInfo.builder()
                        .provider(Provider.Google)
                        .providerId(rootNode.get("id").asText())
                        .email(rootNode.has("email") ? rootNode.get("email").asText() : null)
                        .name(rootNode.has("name") ? rootNode.get("name").asText() : null)
                        .build();
                case Kakao -> {
                    String providerId = rootNode.get("id").asText();
                    String email = null;
                    String name = null;

                    // 닉네임 추출 (kakao_account.profile.nickname 우선, 없으면 properties.nickname)
                    JsonNode kakaoAccount = rootNode.get("kakao_account");
                    if (kakaoAccount != null) {
                        // 이메일 추출
                        if (kakaoAccount.has("email")) {
                            email = kakaoAccount.get("email").asText();
                        }

                        // 닉네임 추출 (kakao_account.profile.nickname)
                        JsonNode profile = kakaoAccount.get("profile");
                        if (profile != null && profile.has("nickname")) {
                            name = profile.get("nickname").asText();
                        }
                    }

                    // kakao_account에 닉네임이 없으면 properties.nickname 사용
                    if (name == null && rootNode.has("properties")) {
                        JsonNode properties = rootNode.get("properties");
                        if (properties.has("nickname")) {
                            name = properties.get("nickname").asText();
                        }
                    }

                    log.info("Kakao 사용자 정보 파싱 완료 - providerId: {}, name: {}, email: {}", providerId, name, email);

                    yield OAuthUserInfo.builder()
                            .provider(Provider.Kakao)
                            .providerId(providerId)
                            .email(email)
                            .name(name)
                            .build();
                }
                default -> throw new BusinessException(ErrorCode.INVALID_OAUTH_PROVIDER);
            };
        } catch (Exception e) {
            log.error("OAuth 사용자 정보 파싱 실패 - provider: {}, error: {}", provider, e.getMessage());
            throw new BusinessException(ErrorCode.OAUTH_USER_INFO_ERROR);
        }
    }
}
