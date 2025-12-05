package groom.backend.domain.users.service.impl;

import groom.backend.common.exception.UserNotFoundException;
import groom.backend.domain.users.dto.request.UpdateUserRequest;
import groom.backend.domain.users.dto.response.UserResponse;
import groom.backend.domain.users.entity.User;
import groom.backend.domain.users.entity.UserCredential;
import groom.backend.domain.users.mapper.UserMapper;
import groom.backend.domain.users.repository.impl.UserCredentialRepositoryImpl;
import groom.backend.domain.users.repository.impl.UserRepositoryImpl;
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

    private final UserRepositoryImpl userRepository;
    private final UserCredentialRepositoryImpl userCredentialRepository;

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
