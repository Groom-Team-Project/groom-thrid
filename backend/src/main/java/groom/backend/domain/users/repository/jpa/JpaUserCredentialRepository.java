package groom.backend.domain.users.repository.jpa;

import groom.backend.domain.users.entity.Provider;
import groom.backend.domain.users.entity.UserCredential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserCredentialRepository extends JpaRepository<UserCredential, UUID> {

    Optional<UserCredential> findByEmail(String email);

    Optional<UserCredential> findByProviderIdAndProvider(String providerId, Provider provider);
}
