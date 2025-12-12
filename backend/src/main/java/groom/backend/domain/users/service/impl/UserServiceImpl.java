package groom.backend.domain.users.service.impl;

import groom.backend.common.exception.BusinessException;
import groom.backend.common.exception.ErrorCode;
import groom.backend.common.exception.UserNotFoundException;
import groom.backend.domain.users.dto.request.UpdateUserRequest;
import groom.backend.domain.users.dto.response.RelationInfoResponse;
import groom.backend.domain.users.dto.response.UserResponse;
import groom.backend.domain.users.entity.User;
import groom.backend.domain.users.entity.UserCredential;
import groom.backend.domain.users.entity.UserRelation;
import groom.backend.domain.users.mapper.UserMapper;
import groom.backend.domain.users.repository.spec.UserCredentialRepository;
import groom.backend.domain.users.repository.spec.UserRelationRepository;
import groom.backend.domain.users.repository.spec.UserRepository;
import groom.backend.domain.users.service.spec.UserService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final UserRelationRepository userRelationRepository;

    @Override
    public UserResponse getUserById(UUID id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return UserMapper.toDto(user);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return UserMapper.toDtoList(users);
    }

    @Override
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

        // Mapper로 엔티티 업데이트
        UserMapper.updateEntity(user, request);

        return UserMapper.toDto(user);
    }

    @Override
    public void deleteUser(UUID id) {
        User user = userRepository.findUserById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        userRepository.deleteById(user.getId());
    }

    @Override
    public void guardianMatch(UUID userId, String email) {

        // 1. userId로 이미 관계가 있는지 확인 (user로써)
        if (userRelationRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS);
        }

        // 2. email로 guardian User 조회
        User guardian = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        // 3. guardian의 ID로 관계가 있는지 확인 (guardian도 다른 누군가의 user인지)
        if (userRelationRepository.existsByUserId(guardian.getId())) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS);
        }

        // 4. user 조회
        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // 5. 관계 생성
        UserRelation relation = UserRelation.create(user, guardian);
        userRelationRepository.save(relation);
    }

    @Override
    public RelationInfoResponse relationInfo(Long relationId) {
        UserRelation userRelation = userRelationRepository.findById(relationId).get();

        return UserMapper.toDto(userRelation);
    }

    // ================= 내부 api용 ===================


    @Override
    public User saveUser(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<UserCredential> findUserCredentialByEmail(String email) {
        return userCredentialRepository.findUserCredentialByEmail(email);
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        return userRepository.findUserById(id);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
