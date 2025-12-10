package groom.backend.domain.users.repository.spec;

import groom.backend.domain.users.entity.UserRelation;
import java.util.Optional;
import java.util.UUID;

public interface UserRelationRepository {

    Optional<UserRelation> findById(Long id);

    // 사용자 ID로 관계 조회
    Optional<UserRelation> findByUserId(UUID userId);

    // 관계 저장
    UserRelation save(UserRelation userRelation);

    // 관계 삭제
    void delete(UserRelation userRelation);

    // 사용자 ID로 관계 존재 여부 확인
    boolean existsByUserId(UUID userId);

    // email로 관계 존재 여부 확인
    boolean existsByEmail(String email);
}
