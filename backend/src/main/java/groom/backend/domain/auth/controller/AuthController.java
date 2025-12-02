package groom.backend.domain.auth.controller;

import groom.backend.domain.users.dto.request.CreateUserRequest;
import groom.backend.domain.users.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authenticate", description = "인증/인가 API")
public class AuthController {

    @PostMapping("/")
    public UserResponse signup(@RequestBody CreateUserRequest createUserRequest) {
        return null;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest loginRequest) {
        return null;
    }

    @PostMapping("/logout")
    public void logout() {
    }

    @PostMapping("/refresh")
    public RefreshResponse refreshToken(@Parameter RefreshToken refreshToken) {
        return null;
    }
}
