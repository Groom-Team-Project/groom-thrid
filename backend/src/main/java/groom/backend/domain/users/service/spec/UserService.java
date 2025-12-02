package groom.backend.domain.users.service.spec;

import groom.backend.domain.users.entity.Role;
import groom.backend.domain.users.entity.User;
import java.util.List;
import java.util.UUID;

public interface UserService {

    User createUser(User user);

    boolean existByEmail(String email);

    List<User> getAllUsers();

    User getUserById(UUID id);

    User updateUser(UpdateUserRequest req);

    void deleteUser(UUID id);

    User updateUserRole(Role role, UUID id);
}
