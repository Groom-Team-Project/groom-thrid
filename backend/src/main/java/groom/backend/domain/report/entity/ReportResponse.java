package groom.backend.domain.report.entity;

import groom.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity(name = "report_response")
@Table(name = "report_response")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ReportResponse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "report_id", nullable = false)
    private Long reportId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(nullable = false, length = 2000)
    private String content;

    @Column(nullable = false, length = 100)
    private String author;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "admin_reply", length = 2000)
    private String adminReply;
}

