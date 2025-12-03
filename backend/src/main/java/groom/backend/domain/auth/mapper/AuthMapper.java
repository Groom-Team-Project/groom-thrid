package groom.backend.domain.auth.mapper;

import groom.backend.domain.auth.dto.response.SignupAuthResponse;
import groom.backend.domain.auth.dto.response.UserInfoDto;
import groom.backend.domain.users.entity.User;

public class AuthMapper {

    public static UserInfoDto toUserInfoDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserInfoDto(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }

    public static SignupAuthResponse toSignupAuthResponse(
            User user,
            String accessToken,
            String refreshToken
    ) {
        UserInfoDto userInfo = toUserInfoDto(user);

        return new SignupAuthResponse(
                userInfo,
                accessToken,
                refreshToken
        );
    }
}
