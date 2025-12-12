package groom.backend.domain.auth.controller;

import groom.backend.domain.auth.dto.request.FormLoginAuthRequest;
import groom.backend.domain.auth.dto.request.FormSignupAuthRequest;
import groom.backend.domain.auth.dto.request.OAuthLoginRequest;
import groom.backend.domain.auth.dto.request.OAuthSignupAuthRequest;
import groom.backend.domain.auth.dto.response.CommonAuthResponse;
import groom.backend.domain.auth.dto.response.SignupAuthResponse;
import groom.backend.domain.auth.service.spec.AuthService;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authenticate", description = "인증/인가 API")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

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
            description = "현재 기기에서 로그아웃 처리합니다. Redis에서 Refresh Token을 삭제하여 무효화합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그아웃 성공"
            )
    })
    @PostMapping("/logout")
    public void logout(
            @Parameter(description = "Bearer {refreshToken}", required = true)
            @RequestHeader("Authorization") String authorizationHeader
    ) {
        String refreshToken = authorizationHeader.replace("Bearer ", "");
        authService.logout(refreshToken);
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
}
