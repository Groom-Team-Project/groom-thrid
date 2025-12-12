package groom.backend.domain.users.service.spec;

import groom.backend.domain.users.dto.request.UpdateUserRequest;
import groom.backend.domain.users.dto.response.RelationInfoResponse;
import groom.backend.domain.users.dto.response.UserResponse;
import groom.backend.domain.users.entity.User;
import groom.backend.domain.users.entity.UserCredential;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

    // ========== Controller용 메서드 (DTO 반환) ==========

    // 사용자 ID로 조회
    UserResponse getUserById(UUID id);

    // 전체 사용자 목록 조회
    List<UserResponse> getAllUsers();

    // 사용자 프로필 수정
    UserResponse updateUser(UUID id, UpdateUserRequest request);

    // 사용자 삭제
    void deleteUser(UUID id);

    // 관계 설정
    void guardianMatch(UUID userId, String email);

    // 관계 조회
    RelationInfoResponse relationInfo(Long relationId);
    // ================= 내부 api용 ===================

    // User 엔티티로 사용자 생성
    User saveUser(User user);

    // ID로 User Entity 조회
    Optional<User> findUserById(UUID id);

    // 이메일 중복 확인 (회원가입 시 사용)
    boolean existsByEmail(String email);

    // 로그인시 UserCredential에서 이메일 확인
    Optional<UserCredential> findUserCredentialByEmail(String email);
}
