package groom.backend.domain.users.repository.jpa;

import groom.backend.domain.users.entity.UserRelation;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JpaUserRelationRepository extends JpaRepository<UserRelation, Long> {

    // 사용자 ID로 관계 조회 (보호자 정보 포함 FETCH JOIN)
    @Query("SELECT ur FROM UserRelation ur JOIN FETCH ur.guardian WHERE ur.user.id = :userId")
    Optional<UserRelation> findByUserIdWithProtector(@Param("userId") UUID userId);

    // 보호자 ID로 관계 조회 (사용자 정보 포함 FETCH JOIN)
    @Query("SELECT ur FROM UserRelation ur JOIN FETCH ur.user WHERE ur.guardian.id = :guardianId")
    Optional<UserRelation> findByGuardianIdWithUser(@Param("guardianId") UUID guardianId);

    // 사용자 ID로 관계 존재 여부 확인
    @Query("SELECT CASE WHEN COUNT(ur) > 0 THEN true ELSE false END FROM UserRelation ur WHERE ur.user.id = :userId")
    boolean existsByUserId(@Param("userId") UUID userId);
}
