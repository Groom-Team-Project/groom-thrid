package groom.backend.domain.auth.controller;

import groom.backend.domain.auth.dto.request.FormLoginAuthRequest;
import groom.backend.domain.auth.dto.response.CommonAuthResponse;
import groom.backend.domain.users.dto.request.CreateUserRequest;
import groom.backend.domain.users.dto.response.UserResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authenticate", description = "인증/인가 API")
public class AuthController {

    @PostMapping("/")
    public UserResponse formSignup(@Valid @RequestBody CreateUserRequest createUserRequest) {
        return null;
    }

    @PostMapping("/form-login")
    public CommonAuthResponse formLogin(@Valid @RequestBody FormLoginAuthRequest req) {
        return null;
    }

    @PostMapping("/logout")
    public void logout(@RequestHeader("Authorization") String authorizationHeader) {
        String refreshToken = authorizationHeader.replace("Bearer ", "");
    }

    @PostMapping("/refresh")
    public CommonAuthResponse refreshToken(@RequestHeader("Authorization") String authorizationHeader) {

        String refreshToken = authorizationHeader.replace("Bearer ", "");

        return null;
    }
}
