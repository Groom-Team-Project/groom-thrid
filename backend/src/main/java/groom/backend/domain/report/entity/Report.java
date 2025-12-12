package groom.backend.domain.report.entity;

import groom.backend.common.entity.BaseEntity;
import groom.backend.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

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

    // 작성자 (User 엔티티와 연결)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 작성자 이름 (조회 성능을 위한 denormalized 필드)
    @Column(nullable = false, length = 100)
    private String author;

    // 제보 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    // 이미지 URL
    @Column(name = "image_url", length = 100000)
    private String imageUrl;

    // 관리자 답변 (승인/반려 시 사용자에게 전달)
    @Column(name = "admin_reply", length = 2000)
    private String adminReply;

    // 제보 수정 메서드
    public void update(String content, String imageUrl) {
        this.content = content;
        this.imageUrl = imageUrl;
    }

    // 상태 변경 메서드
    public void changeStatus(ReportStatus status) {
        this.status = status;
    }

    // 관리자 상태 변경 및 답변 메서드
    public void updateStatusWithReply(ReportStatus status, String adminReply) {
        this.status = status;
        this.adminReply = adminReply;
    }
}


