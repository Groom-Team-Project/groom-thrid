package groom.backend.domain.report.entity;

import groom.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "report")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class Report extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ChargerLocation의 placeId 참조
    @Column(nullable = false)
    private Long placeId;

    // 제보 내용
    @Column(nullable = false, length = 2000)
    private String content;

    // 작성자 (추후 User 엔티티와 연결 할 것임)
    @Column(nullable = false, length = 100)
    private String author;

    // 제보 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    // 이미지 URL
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // 제보 수정 메서드
    public void update(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
    }

    // 상태 변경 메서드
    public void changeStatus(ReportStatus status) {
        this.status = status;
    }
}

