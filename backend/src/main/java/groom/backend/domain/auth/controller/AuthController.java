package groom.backend.domain.auth.controller;

import groom.backend.domain.auth.dto.request.FormLoginAuthRequest;
import groom.backend.domain.auth.dto.request.FormSignupAuthRequest;
import groom.backend.domain.auth.dto.request.OAuthLoginRequest;
import groom.backend.domain.auth.dto.request.OAuthSignupAuthRequest;
import groom.backend.domain.auth.dto.response.CommonAuthResponse;
import groom.backend.domain.auth.dto.response.SignupAuthResponse;
import groom.backend.domain.auth.service.spec.AuthService;
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

    // form 회원가입
    @PostMapping("/form-signup")
    public SignupAuthResponse formSignup(
            @Valid @RequestBody FormSignupAuthRequest req,
            HttpServletResponse res
    ) {

        SignupAuthResponse result = authService.formSignup(req);
        res.setStatus(201);

        return result;
    }

    //
    @PostMapping("/oauth-signup")
    public SignupAuthResponse oauthSignup(@Valid @RequestBody OAuthSignupAuthRequest req,
                                          HttpServletResponse res) {

        SignupAuthResponse result = authService.oauthSignup(req);

        res.setStatus(201);

        return result;
    }

    // 로그인(form)
    @PostMapping("/form-login")
    public CommonAuthResponse formLogin(@Valid @RequestBody FormLoginAuthRequest req) {
        return authService.formLogin(req);
    }

    // 로그인(OAuth)
    @PostMapping("/oauth-login")
    public CommonAuthResponse oauthLogin(@Valid @RequestBody OAuthLoginRequest req) {
        return authService.oauthLogin(req);
    }

    // 로그아웃(토큰 무효화)
    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.replace("Bearer ", "");
        authService.logout(refreshToken);
    }

    // access 토큰 재발급
    @PostMapping("/refresh")
    public CommonAuthResponse refreshToken(@RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.replace("Bearer ", "");
        return authService.refreshToken(refreshToken);
    }
}
