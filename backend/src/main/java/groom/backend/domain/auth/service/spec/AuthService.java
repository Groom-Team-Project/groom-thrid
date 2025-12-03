package groom.backend.domain.auth.service.spec;

import groom.backend.domain.auth.dto.request.FormLoginAuthRequest;
import groom.backend.domain.auth.dto.request.FormSignupAuthRequest;
import groom.backend.domain.auth.dto.response.CommonAuthResponse;
import groom.backend.domain.auth.dto.response.SignupAuthResponse;

public interface AuthService {

    SignupAuthResponse formSignup(FormSignupAuthRequest req);

    CommonAuthResponse formLogin(FormLoginAuthRequest req);

    void logout(String refreshToken);

    CommonAuthResponse refreshToken(String refreshToken);
}
