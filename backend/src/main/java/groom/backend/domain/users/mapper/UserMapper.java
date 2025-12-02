package groom.backend.domain.users.mapper;

import groom.backend.domain.users.dto.request.CreateUserRequest;
import groom.backend.domain.users.dto.response.UserResponse;
import groom.backend.domain.users.entity.Role;
import groom.backend.domain.users.entity.User;
import java.util.List;
import java.util.stream.Collectors;

public class UserMapper {

    // Entity → DTO 변환
    public static UserResponse toDto(User user) {
        if (user == null) {
            return null;
        }

        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    // Entity → DTO List 변환
    public static List<UserResponse> toDtoList(List<User> users) {
        if (users == null) {
            return null;
        }

        return users.stream()
                .map(UserMapper::toDto)
                .collect(Collectors.toList());
    }

    // Request DTO → Entity 변환
    public static User toEntity(CreateUserRequest request) {
        if (request == null) {
            return null;
        }

        Role role = request.role() != null ? request.role() : Role.USER;

        return User.createUser(
                request.name(),
                request.phone(),
                role,
                request.email()
        );
    }
}
