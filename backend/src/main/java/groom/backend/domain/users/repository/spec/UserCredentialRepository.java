package groom.backend.domain.users.repository.spec;

import groom.backend.domain.users.entity.Provider;
import groom.backend.domain.users.entity.UserCredential;
import java.util.Optional;

public interface UserCredentialRepository {

    // Form 용
    Optional<UserCredential> findByEmail(String email);

    // OAuth용
    Optional<UserCredential> findByProviderIdAndProvider(String providerId, Provider provider);

    UserCredential save(UserCredential userCredential);
}
