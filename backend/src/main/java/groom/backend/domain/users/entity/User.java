package groom.backend.domain.users.entity;

import groom.backend.common.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.HashSet;
import java.util.Set;
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

    @Column(length = 100)
    private String contactEmail;

    @Column(nullable = false)
    private String phone;

    @Column
    private boolean active = true;

    // User 1:N UserCredential 관계
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<UserCredential> credentials = new HashSet<>();

    @Column(nullable = false)
    private Role role = Role.USER;

    // User 생성(팩토리 패턴)
    public static User createUser(String contactEmail, String name, String phone, Role role) {
        User user = new User();
        user.contactEmail = contactEmail;
        user.name = name;
        user.phone = phone;
        user.active = true;
        user.role = role != null ? role : Role.USER;
        return user;
    }

    // OAuth 방식 User 생성(보류)
    public static User createOAuthUser(String contactEmail, String name){
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
    public void updateProfile(String name, String phone, String contactEmail) {
        if (name != null) this.name = name;
        if (phone != null) this.phone = phone;
        if (contactEmail != null) this.contactEmail = contactEmail;
    }

    // Credential 추가 (양방향 관계 동기화)
    public void addCredential(UserCredential credential) {
        this.credentials.add(credential);
        credential.setUser(this);
    }

    // Credential 제거 (양방향 관계 동기화)
    public void removeCredential(UserCredential credential) {
        this.credentials.remove(credential);
        credential.setUser(null);
    }
}
