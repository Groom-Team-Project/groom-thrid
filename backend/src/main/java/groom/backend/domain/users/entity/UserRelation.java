package groom.backend.domain.users.entity;

import groom.backend.common.entity.BaseEntity;
import groom.backend.domain.notification.entity.Notification;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserRelation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(nullable = false)
    private User guardian;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "relation")
    private List<Notification> notifications;

    // 사용자-보호자 관계 생성
    public static UserRelation create(User user, User guardian) {
        if (user == null || guardian == null) {
            throw new IllegalArgumentException("사용자와 보호자는 null일 수 없습니다");
        }
        if (user.getId().equals(guardian.getId())) {
            throw new IllegalArgumentException("사용자와 보호자는 동일한 사람일 수 없습니다");
        }

        UserRelation relation = new UserRelation();
        relation.user = user;
        relation.guardian = guardian;
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
        this.guardian = newProtector;
    }

    // 보호자 ID 조회
    public UUID getProtectorId() {
        return this.guardian.getId();
    }

    // 사용자 ID 조회
    public UUID getUserId() {
        return this.user.getId();
    }

    // 알림 추가
    public void addNotification(Notification notification) {
        this.notifications.add(notification);
        notification.setUserRelation(this);
    }
}
