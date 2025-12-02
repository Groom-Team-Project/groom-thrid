package groom.backend.domain.users.service.spec;

import groom.backend.domain.users.dto.request.UpdateUserRequest;
import groom.backend.domain.users.dto.response.UserResponse;
import groom.backend.domain.users.entity.Role;
import groom.backend.domain.users.entity.User;
import java.util.List;
import java.util.UUID;

public interface UserService {

    // ========== Controller용 메서드 (DTO 반환) ==========

    /**
     * 사용자 ID로 조회
     */
    UserResponse getUserById(UUID id);

    /**
     * 전체 사용자 목록 조회
     */
    List<UserResponse> getAllUsers();

    /**
     * 사용자 프로필 수정
     */
    UserResponse updateUser(UUID id, UpdateUserRequest request);

    /**
     * 사용자 삭제
     */
    void deleteUser(UUID id);

    /**
     * 역할 변경
     */
    UserResponse updateUserRole(UUID id, Role role);

    // ========== 다른 Service용 메서드 (Entity 반환) ==========

    /**
     * ID로 User Entity 조회 (내부 도메인용)
     */
    User findUserEntityById(UUID id);

    /**
     * 이메일로 User Entity 조회 (내부 도메인용) - UserDetailsService에서 사용 (Spring Security 인증) - Auth 도메인에서 사용 (토큰 생성 등)
     */
    User findUserEntityByEmail(String email);

    // ========== 유틸리티 메서드 ==========

    /**
     * 이메일 중복 확인 (회원가입 시 사용)
     */
    boolean existsByEmail(String email);
}
