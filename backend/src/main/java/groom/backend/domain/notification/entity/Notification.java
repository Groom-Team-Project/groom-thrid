package groom.backend.domain.notification.entity;

import groom.backend.common.entity.BaseEntity;
import groom.backend.domain.users.entity.UserRelation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "notifications")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_id", nullable = false)
    private UserRelation relation;

    @Schema(description = "위도")
    private Double lat;

    @Schema(description = "경도")
    private Double lng;

    @Schema(description = "도로명 주소")
    private String address;

    @Schema(description = "보호자 알림 확인여부")
    private boolean isCheck;

    @Schema(description = "보호자 알림 확인 시간")
    private LocalDateTime checkedAt;
}
