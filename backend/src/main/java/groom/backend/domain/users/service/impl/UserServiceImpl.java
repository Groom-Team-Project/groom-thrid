package groom.backend.domain.users.service.impl;

import groom.backend.common.exception.BusinessException;
import groom.backend.common.exception.ErrorCode;
import groom.backend.common.exception.UserNotFoundException;
import groom.backend.domain.users.dto.request.UpdateUserRequest;
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

        // 관계 여부 검증
        if (userRelationRepository.existsByUserId(userId)) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS);
        }

        if (userRelationRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.RELATION_EXISTS);
        }

        User user = userRepository.findUserById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        User guardian = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));

        UserRelation relation = UserRelation.create(user, guardian);
        userRelationRepository.save(relation);
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
