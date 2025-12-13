package groom.backend.domain.users.mapper;

import groom.backend.common.exception.BusinessException;
import groom.backend.common.exception.ErrorCode;
import groom.backend.domain.users.dto.request.CreateUserRequest;
import groom.backend.domain.users.dto.request.UpdateUserRequest;
import groom.backend.domain.users.dto.response.RelationInfoResponse;
import groom.backend.domain.users.dto.response.UserResponse;
import groom.backend.domain.users.entity.Role;
import groom.backend.domain.users.entity.User;
import groom.backend.domain.users.entity.UserRelation;
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

    public static void updateEntity(User user, UpdateUserRequest request) {
        if (request == null) {
            return;
        }

        user.updateProfile(request.name(), request.phone());

        user.changeRole(request.role());
    }

    public static RelationInfoResponse toDto(UserRelation userRelation) {

        if (userRelation == null) {
            throw new BusinessException(ErrorCode.RELATION_NOT_FOUND);
        }

        return new RelationInfoResponse(
                userRelation.getUser().getName(),
                userRelation.getUser().getEmail(),
                userRelation.getGuardian().getName(),
                userRelation.getGuardian().getEmail()
        );
    }
}
