package groom.backend.domain.auth.service.impl;

import groom.backend.common.exception.BusinessException;
import groom.backend.common.exception.ErrorCode;
import groom.backend.common.util.JwtUtil;
import groom.backend.domain.auth.dto.request.FormLoginAuthRequest;
import groom.backend.domain.auth.dto.response.CommonAuthResponse;
import groom.backend.domain.auth.dto.response.SignupAuthResponse;
import groom.backend.domain.auth.mapper.AuthMapper;
import groom.backend.domain.auth.service.spec.AuthService;
import groom.backend.domain.users.dto.request.CreateUserRequest;
import groom.backend.domain.users.entity.User;
import groom.backend.domain.users.entity.UserCredential;
import groom.backend.domain.users.repository.impl.UserCredentialRepositoryImpl;
import groom.backend.domain.users.repository.impl.UserRepositoryImpl;
import groom.backend.domain.users.service.impl.UserServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserServiceImpl userService;
    private final UserRepositoryImpl userRepository;
    private final UserCredentialRepositoryImpl credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public SignupAuthResponse formSignup(CreateUserRequest req) {

        // 1. 이메일 중복 검사
        if (userRepository.existsByEmail(req.email())) {
            // 에러코드 설정
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        // 2. User 엔티티 객체 생성
        User user = User.createUser(
                req.name(),
                req.phone(),
                req.role(),
                req.email()
        );

        // 3. 비밀번호 암호화 -> Bcrypt 암호화
        String encodedPassword = passwordEncoder.encode(req.password());

        // 4. UserCredential 엔티티 객체 생성
        UserCredential credential = UserCredential.createFormCredential(
                user,
                req.email(),
                encodedPassword
        );

        // 5. User에 양방향 관계 설정
        user.setCredential(credential);

        // 6. DB 저장
        User newUser = userRepository.save(user);

        // 7. JWT 토큰 생성 (userId + role 포함)
        String accessToken = jwtUtil.generateAccessToken(newUser.getId(), newUser.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(newUser.getId());

        // 8. DTO 변환 및 반환
        return AuthMapper.toSignupAuthResponse(newUser, accessToken, refreshToken);
    }

    @Override
    public CommonAuthResponse formLogin(FormLoginAuthRequest req) {
        // 1. 이메일로 UserCredential 조회
        UserCredential credential = credentialRepository.findByEmail(req.email())
                .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));

        // 2. 비밀번호 검증
        if (!passwordEncoder.matches(req.password(), credential.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }

        // 3. 사용자 정보 조회
        User user = credential.getUser();

        // 4. 계정 활성화 여부 확인
        if (!user.isActive()) {
            throw new BusinessException(ErrorCode.DEACTIVATED_USER);
        }

        // 5. JWT 토큰 생성 (userId + role 포함)
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 6. 토큰 반환
        return new CommonAuthResponse(accessToken, refreshToken);
    }

    @Override
    public void logout(String refreshToken) {

    }

    @Override
    public CommonAuthResponse refreshToken(String refreshToken) {
        return null;
    }
}
