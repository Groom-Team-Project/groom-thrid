package groom.backend.domain.users.repository.impl;

import groom.backend.domain.users.entity.Provider;
import groom.backend.domain.users.entity.UserCredential;
import groom.backend.domain.users.repository.spec.UserCredentialRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserCredentialRepositoryImpl implements UserCredentialRepository {

    private final UserCredentialRepository userCredentialRepository;

    @Override
    public Optional<UserCredential> findByEmail(String email) {
        return userCredentialRepository.findByEmail(email);
    }

    @Override
    public Optional<UserCredential> findByProviderIdAndProvider(String providerId, Provider provider) {
        return userCredentialRepository.findByProviderIdAndProvider(providerId, provider);
    }

    @Override
    public UserCredential save(UserCredential userCredential) {
        return userCredentialRepository.save(userCredential);
    }
}
