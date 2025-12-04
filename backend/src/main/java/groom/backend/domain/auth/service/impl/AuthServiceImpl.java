package groom.backend.domain.auth.service.impl;

import groom.backend.common.exception.BusinessException;
import groom.backend.common.exception.ErrorCode;
import groom.backend.common.util.JwtUtil;
import groom.backend.domain.auth.dto.request.FormLoginAuthRequest;
import groom.backend.domain.auth.dto.request.FormSignupAuthRequest;
import groom.backend.domain.auth.dto.response.CommonAuthResponse;
import groom.backend.domain.auth.dto.response.SignupAuthResponse;
import groom.backend.domain.auth.mapper.AuthMapper;
import groom.backend.domain.auth.repository.impl.RefreshTokenRedisRepositoryImpl;
import groom.backend.domain.auth.service.spec.AuthService;
import groom.backend.domain.auth.vo.RefreshTokenValue;
import groom.backend.domain.users.entity.User;
import groom.backend.domain.users.entity.UserCredential;
import groom.backend.domain.users.service.impl.UserServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

    private final UserServiceImpl userService;
    private final RefreshTokenRedisRepositoryImpl refreshTokenRedisRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // secret에서 만료시간 가져오기
    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    public SignupAuthResponse formSignup(FormSignupAuthRequest req) {

        // 1. 이메일 중복 검사
        if (userService.existsByEmail(req.email())) {
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
        User newUser = userService.saveUser(user);

        // 7. JWT 토큰 생성 (userId + role 포함)
        String accessToken = jwtUtil.generateAccessToken(newUser.getId(), newUser.getRole());
        String refreshTokenString = jwtUtil.generateRefreshToken(newUser.getId());

        // 8. RefreshToken Redis에 저장 (TTL 자동 적용)
        RefreshTokenValue refreshTokenValue = RefreshTokenValue.of(newUser.getId());
        refreshTokenRedisRepository.save(refreshTokenString, refreshTokenValue);

        // 9. DTO 변환 및 반환
        return AuthMapper.toSignupAuthResponse(newUser, accessToken, refreshTokenString);
    }

    @Override
    public CommonAuthResponse formLogin(FormLoginAuthRequest req) {
        // 1. 이메일로 UserCredential 조회
        UserCredential credential = userService.findUserCredentialByEmail(req.email())
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
        String refreshTokenString = jwtUtil.generateRefreshToken(user.getId());

        // 6. RefreshToken Redis에 저장 (TTL 자동 적용)
        RefreshTokenValue refreshTokenValue = RefreshTokenValue.of(user.getId());
        refreshTokenRedisRepository.save(refreshTokenString, refreshTokenValue);

        // 7. 토큰 반환
        return new CommonAuthResponse(accessToken, refreshTokenString);
    }

    /**
     * 로그아웃 (현재 기기) - RefreshToken을 Redis에서 삭제하여 무효화 - Access Token은 만료 시간까지 유효 (단기간이므로 문제 없음)
     *
     * @param refreshTokenString Refresh Token 문자열
     */
    @Override
    public void logout(String refreshTokenString) {
        // RefreshToken 존재 여부 확인 후 삭제
        if (refreshTokenRedisRepository.existsByToken(refreshTokenString)) {
            refreshTokenRedisRepository.deleteByToken(refreshTokenString);
        }
        // 존재하지 않아도 예외 발생 안함 (이미 로그아웃되었거나 만료됨)
    }

    /**
     * Access Token 갱신 - Refresh Token으로 새로운 Access Token 발급 - Refresh Token도 새로 발급하여 Rotation 적용 (보안 강화)
     *
     * @param refreshTokenString Refresh Token 문자열
     * @return 새로운 Access Token + Refresh Token
     */
    @Override
    public CommonAuthResponse refreshToken(String refreshTokenString) {
        // 1. RefreshToken Redis에서 조회
        RefreshTokenValue refreshTokenValue = refreshTokenRedisRepository.findByToken(refreshTokenString)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

        // 2. JWT 토큰 자체 검증 (서명 확인)
        // Redis는 TTL로 자동 만료되므로 별도 만료 확인 불필요
        if (!jwtUtil.validateToken(refreshTokenString)) {
            refreshTokenRedisRepository.deleteByToken(refreshTokenString);
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        // 3. 사용자 정보 조회
        User user = userService.findUserById(refreshTokenValue.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        // 4. 계정 활성화 여부 확인
        if (!user.isActive()) {
            refreshTokenRedisRepository.deleteByToken(refreshTokenString);
            throw new BusinessException(ErrorCode.DEACTIVATED_USER);
        }

        // 5. 새로운 Access Token 생성 (최신 role 반영)
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getRole());

        // 6. 새로운 Refresh Token 생성 (Refresh Token Rotation)
        String newRefreshTokenString = jwtUtil.generateRefreshToken(user.getId());

        // 7. 기존 RefreshToken 삭제 후 새 토큰 저장 (Rotation)
        refreshTokenRedisRepository.deleteByToken(refreshTokenString);
        RefreshTokenValue newRefreshTokenValue = RefreshTokenValue.of(user.getId());
        refreshTokenRedisRepository.save(newRefreshTokenString, newRefreshTokenValue);

        // 8. 새로운 토큰 반환
        return new CommonAuthResponse(newAccessToken, newRefreshTokenString);
    }
}
