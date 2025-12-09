package groom.backend.domain.users.repository.impl;

import groom.backend.domain.users.entity.UserRelation;
import groom.backend.domain.users.repository.jpa.JpaUserRelationRepository;
import groom.backend.domain.users.repository.spec.UserRelationRepository;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * UserRelation Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class UserRelationRepositoryImpl implements UserRelationRepository {

    private final JpaUserRelationRepository jpaUserRelationRepository;

    @Override
    public Optional<UserRelation> findById(Long relationId) {
        return jpaUserRelationRepository.findById(relationId);
    }

    @Override
    public Optional<UserRelation> findByUserId(UUID userId) {
        return jpaUserRelationRepository.findByUserIdWithProtector(userId);
    }

    @Override
    public UserRelation save(UserRelation userRelation) {
        return jpaUserRelationRepository.save(userRelation);
    }

    @Override
    public void delete(UserRelation userRelation) {
        jpaUserRelationRepository.delete(userRelation);
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaUserRelationRepository.existsByUserId(userId);
    }
}
