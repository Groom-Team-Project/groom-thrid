package groom.backend.domain.auth.service.spec;

import groom.backend.domain.auth.dto.response.CommonAuthResponse;

public interface AuthService {

    public CommonAuthResponse formSignup();

    public CommonAuthResponse formLogin();

    public void logout(String refreshToken);
}
