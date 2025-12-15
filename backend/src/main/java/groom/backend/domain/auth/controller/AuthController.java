package groom.backend.domain.auth.controller;

import groom.backend.common.exception.BusinessException;
import groom.backend.common.exception.ErrorCode;
import groom.backend.domain.auth.dto.request.FormLoginAuthRequest;
import groom.backend.domain.auth.dto.request.FormSignupAuthRequest;
import groom.backend.domain.auth.dto.request.OAuthLoginRequest;
import groom.backend.domain.auth.dto.request.OAuthSignupAuthRequest;
import groom.backend.domain.auth.dto.response.CommonAuthResponse;
import groom.backend.domain.auth.dto.response.OAuthUserInfo;
import groom.backend.domain.auth.dto.response.SignupAuthResponse;
import groom.backend.domain.auth.service.spec.AuthService;
import groom.backend.domain.auth.service.spec.OAuthService;
import groom.backend.domain.users.entity.Provider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authenticate", description = "인증/인가 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final OAuthService oauthService;

    @Value("${frontend.url:http://localhost:3000}")
    private String frontendUrl;

    @Operation(
            summary = "Form 회원가입",
            description = "이메일과 비밀번호를 사용한 일반 회원가입을 처리합니다. 회원가입 성공 시 Access Token과 Refresh Token을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = SignupAuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검증 실패)"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이메일 중복"
            )
    })
    @PostMapping("/form-signup")
    public SignupAuthResponse formSignup(
            @Valid @RequestBody FormSignupAuthRequest req,
            HttpServletResponse res
    ) {

        SignupAuthResponse result = authService.formSignup(req);
        res.setStatus(201);

        return result;
    }

    @Operation(
            summary = "OAuth 회원가입",
            description = "소셜 로그인(Naver, Google, Kakao)을 통한 회원가입을 처리합니다. 회원가입 성공 시 Access Token과 Refresh Token을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = SignupAuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 (유효성 검증 실패)"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이메일 중복"
            )
    })
    @PostMapping("/oauth-signup")
    public SignupAuthResponse oauthSignup(@Valid @RequestBody OAuthSignupAuthRequest req,
                                          HttpServletResponse res) {

        SignupAuthResponse result = authService.oauthSignup(req);

        res.setStatus(201);

        return result;
    }

    @Operation(
            summary = "Form 로그인",
            description = "이메일과 비밀번호를 사용한 일반 로그인을 처리합니다. 로그인 성공 시 Access Token과 Refresh Token을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = CommonAuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 (이메일 또는 비밀번호 불일치)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "비활성화된 계정"
            )
    })
    @PostMapping("/form-login")
    public CommonAuthResponse formLogin(@Valid @RequestBody FormLoginAuthRequest req) {
        return authService.formLogin(req);
    }

    @Operation(
            summary = "OAuth 로그인",
            description = "소셜 로그인(Naver, Google, Kakao)을 통한 로그인을 처리합니다. 로그인 성공 시 Access Token과 Refresh Token을 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = CommonAuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음 (회원가입 필요)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "비활성화된 계정"
            )
    })
    @PostMapping("/oauth-login")
    public CommonAuthResponse oauthLogin(@Valid @RequestBody OAuthLoginRequest req) {
        return authService.oauthLogin(req);
    }

    @Operation(
            summary = "로그아웃",
            description = "현재 기기에서 로그아웃 처리합니다. Access Token을 블랙리스트에 추가하고 Refresh Token을 삭제하여 무효화합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            )
    })
    @PostMapping("/logout")
    public void logout(
            @Parameter(description = "Bearer {accessToken}", required = true)
            @RequestHeader("Authorization") String accessTokenHeader,
            @Parameter(description = "Bearer {refreshToken}", required = true)
            @RequestHeader("X-Refresh-Token") String refreshTokenHeader
    ) {
        String accessToken = accessTokenHeader.replace("Bearer ", "");
        String refreshToken = refreshTokenHeader.replace("Bearer ", "");
        authService.logout(accessToken, refreshToken);
    }

    @Operation(
            summary = "Access Token 재발급",
            description = "Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급합니다. Refresh Token Rotation을 적용하여 보안을 강화합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "토큰 재발급 성공",
                    content = @Content(schema = @Schema(implementation = CommonAuthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "유효하지 않은 Refresh Token"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Refresh Token을 찾을 수 없음 (만료되었거나 로그아웃됨)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "비활성화된 계정"
            )
    })
    @PostMapping("/refresh")
    public CommonAuthResponse refreshToken(
            @Parameter(description = "Bearer {refreshToken}", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String refreshToken = authorizationHeader.replace("Bearer ", "");
        return authService.refreshToken(refreshToken);
    }

    @Operation(
            summary = "OAuth 콜백 처리",
            description = "OAuth provider로부터 콜백을 받아 로그인 또는 회원가입을 처리합니다. 사용자가 없으면 프론트엔드 회원가입 페이지로 리다이렉트합니다."
    )
    @GetMapping("/oauth/callback/{provider}")
    public void oauthCallback(
            @PathVariable("provider") String providerName,
            @RequestParam("code") String code,
            HttpServletResponse response
    ) throws IOException {
        try {
            // Provider 파싱
            Provider provider = Provider.valueOf(providerName.substring(0, 1).toUpperCase() + providerName.substring(1).toLowerCase());

            // OAuth 로그인 처리
            CommonAuthResponse authResponse = oauthService.processOAuthCallback(provider, code);

            // 로그인 성공 - 프론트엔드로 리다이렉트하며 토큰 전달
            String redirectUrl = String.format("%s/auth/oauth/success?accessToken=%s&refreshToken=%s",
                    frontendUrl,
                    authResponse.accessToken(),
                    authResponse.refreshToken()
            );
            response.sendRedirect(redirectUrl);

        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.USER_NOT_FOUND) {
                // 사용자 없음 - OAuth 사용자 정보 조회 후 회원가입 페이지로 리다이렉트
                handleSignupRedirect(providerName, code, response);
            } else {
                // 기타 에러 - 에러 페이지로 리다이렉트
                String errorRedirectUrl = String.format("%s/auth?error=%s",
                        frontendUrl,
                        e.getMessage()
                );
                response.sendRedirect(errorRedirectUrl);
            }
        } catch (Exception e) {
            // 예외 발생 - 에러 페이지로 리다이렉트
            String errorRedirectUrl = String.format("%s/auth?error=%s",
                    frontendUrl,
                    "OAuth 처리 중 오류가 발생했습니다"
            );
            response.sendRedirect(errorRedirectUrl);
        }
    }

    /**
     * OAuth 회원가입이 필요한 경우 프론트엔드 회원가입 페이지로 리다이렉트
     */
    private void handleSignupRedirect(String providerName, String code, HttpServletResponse response) throws IOException {
        try {
            Provider provider = Provider.valueOf(providerName.substring(0, 1).toUpperCase() + providerName.substring(1).toLowerCase());

            // Access Token 발급
            String accessToken = oauthService.getAccessToken(provider, code);

            // 사용자 정보 조회
            OAuthUserInfo userInfo = oauthService.getUserInfo(provider, accessToken);

            // 프론트엔드 회원가입 페이지로 리다이렉트 (사용자 정보 포함)
            String signupRedirectUrl = String.format(
                    "%s/auth/oauth/signup?provider=%s&providerId=%s&email=%s&name=%s",
                    frontendUrl,
                    userInfo.getProvider().name(),
                    userInfo.getProviderId(),
                    userInfo.getEmail() != null ? userInfo.getEmail() : "",
                    userInfo.getName() != null ? userInfo.getName() : ""
            );
            response.sendRedirect(signupRedirectUrl);
        } catch (Exception e) {
            String errorRedirectUrl = String.format("%s/auth?error=%s",
                    frontendUrl,
                    "OAuth 사용자 정보 조회에 실패했습니다"
            );
            response.sendRedirect(errorRedirectUrl);
        }
    }
}
