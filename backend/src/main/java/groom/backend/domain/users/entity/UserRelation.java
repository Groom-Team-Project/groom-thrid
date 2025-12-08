package groom.backend.domain.users.entity;

import groom.backend.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_relations")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRelation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protector_id", nullable = false)
    private User protector;

    // 사용자-보호자 관계 생성
    public static UserRelation create(User user, User protector) {
        if (user == null || protector == null) {
            throw new IllegalArgumentException("사용자와 보호자는 null일 수 없습니다");
        }
        if (user.getId().equals(protector.getId())) {
            throw new IllegalArgumentException("사용자와 보호자는 동일한 사람일 수 없습니다");
        }

        UserRelation relation = new UserRelation();
        relation.user = user;
        relation.protector = protector;
        return relation;
    }

    // 보호자 변경
    public void changeProtector(User newProtector) {
        if (newProtector == null) {
            throw new IllegalArgumentException("보호자는 null일 수 없습니다");
        }
        if (this.user.getId().equals(newProtector.getId())) {
            throw new IllegalArgumentException("사용자와 보호자는 동일한 사람일 수 없습니다");
        }
        this.protector = newProtector;
    }

    // 보호자 ID 조회
    public UUID getProtectorId() {
        return this.protector.getId();
    }

    // 사용자 ID 조회
    public UUID getUserId() {
        return this.user.getId();
    }
}
