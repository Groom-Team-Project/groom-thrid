package groom.backend.domain.users.repository.spec;

import groom.backend.domain.users.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository {

    Optional<User> findById(UUID id);

    Boolean existsByEmail(String email);
    
    User save(User user);

    List<User> findAll();

    void deleteById(UUID id);
}
