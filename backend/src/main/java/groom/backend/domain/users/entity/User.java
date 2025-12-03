package groom.backend.domain.users.entity;

import groom.backend.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "email", unique = true)
    private String email;

    @Column(length = 13)
    private String phone;

    @Column
    private boolean active = true;

    // User 1:1 UserCredential 관계
    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private UserCredential credential;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role = Role.USER;

    // User 생성(팩토리 패턴)
    public static User createUser(String name, String phone, Role role, String email) {
        User user = new User();
        user.name = name;
        user.phone = phone;
        user.active = true;
        user.email = email;
        user.role = role != null ? role : Role.USER;
        return user;
    }

    // OAuth 방식 User 생성(보류)
    public static User createOAuthUser(String contactEmail, String name) {
        return new User();
    }

    // role 변경
    public void changeRole(Role role) {
        this.role = role;
    }

    // 계정 비활성화
    public void deactivate() {
        this.active = false;
    }

    // 계정 활성화
    public void activate() {
        this.active = true;
    }

    // 사용자 정보 수정
    public void updateProfile(String name, String phone) {
        if (name != null) {
            this.name = name;
        }
        if (phone != null) {
            this.phone = phone;
        }
    }

    // Credential 설정 (양방향 관계 동기화)
    public void setCredential(UserCredential credential) {
        this.credential = credential;
        if (credential != null) {
            credential.setUser(this);
        }
    }
}
