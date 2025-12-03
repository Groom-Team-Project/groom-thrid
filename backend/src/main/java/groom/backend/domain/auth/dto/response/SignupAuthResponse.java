package groom.backend.domain.auth.dto.response;

public record SignupAuthResponse(
        UserInfoDto user,
        String accessToken,
        String refreshToken
) {
}
