package groom.backend.domain.notification.entity;

import groom.backend.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    UUID id;

    @Schema(description = "위도")
    Double lat;

    @Schema(description = "경도")
    Double lng;

    @Schema(description = "도로명 주소")
    String address;

    @Schema(description = "보호자 알림 확인여부")
    boolean isCheck;

    @Schema(description = "보호자 알림 확인 시간")
    LocalDateTime checkTime;
}
