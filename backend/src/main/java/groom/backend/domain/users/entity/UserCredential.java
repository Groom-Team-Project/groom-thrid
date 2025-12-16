package groom.backend.domain.users.entity;

import groom.backend.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "user_credentials",
        uniqueConstraints = {
                @jakarta.persistence.UniqueConstraint(
                        name = "uk_provider_providerId",
                        columnNames = {"provider", "providerId"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserCredential extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    @Setter
    private User user;

    @Column(nullable = false)
    private Provider provider;

    @Column(name = "provider_id")
    private String providerId;

    @Column(name = "email", unique = true)
    private String email;

    @Column
    private String password;

    // Form용 로그인정보 Credential 생성
    public static UserCredential createFormCredential(User user, String email, String encodedPassword) {
        UserCredential credential = new UserCredential();
        credential.user = user;
        credential.provider = Provider.Form;
        credential.email = email;
        credential.password = encodedPassword;
        return credential;
    }

    // OAuth용 로그인정보 Credential 생성 (이메일 없이)
    public static UserCredential createOAuthCredential(User user, Provider provider, String providerId) {
        if (provider == Provider.Form) {
            throw new IllegalArgumentException("Form provider는 createFormCredential을 사용해야 합니다");
        }
        UserCredential credential = new UserCredential();
        credential.user = user;
        credential.provider = provider;
        credential.providerId = providerId;
        return credential;
    }

    // OAuth용 로그인정보 Credential 생성 (이메일 포함)
    public static UserCredential createOAuthCredential(User user, Provider provider, String providerId, String email) {
        if (provider == Provider.Form) {
            throw new IllegalArgumentException("Form provider는 createFormCredential을 사용해야 합니다");
        }
        UserCredential credential = new UserCredential();
        credential.user = user;
        credential.provider = provider;
        credential.providerId = providerId;
        credential.email = email; // 이메일 설정 (null 가능)
        return credential;
    }

    // 비밀번호 업데이트 (이미 암호화된 비밀번호를 받음)
    public void updatePassword(String encodedPassword) {
        if (this.provider != Provider.Form) {
            throw new IllegalStateException("OAuth 계정은 비밀번호를 변경할 수 없습니다");
        }
        this.password = encodedPassword;
    }
}
