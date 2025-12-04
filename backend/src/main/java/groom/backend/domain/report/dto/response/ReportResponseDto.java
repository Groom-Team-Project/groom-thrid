package groom.backend.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(
    description = "제보 응답 DTO",
    example = "{\"id\": 1, \"placeId\": 1, \"content\": \"장소에 문제가 있습니다!\", \"author\": \"홍길동\", \"status\": \"대기 중\", \"imageUrl\": \"https://example.com/image.jpg\", \"adminReply\": \"제보해주셔서 감사합니다.\", \"createdAt\": \"2025-01-20T10:30:00\", \"updatedAt\": \"2025-01-20T10:30:00\"}"
)
public class ReportResponseDto {

    @Schema(description = "제보 ID", example = "1")
    private Long id;

    @Schema(description = "장소 ID", example = "1")
    private Long placeId;

    @Schema(description = "제보 내용", example = "장소에 문제가 있습니다!")
    private String content;

    @Schema(description = "작성자", example = "홍길동")
    private String author;

    @Schema(description = "제보 상태", example = "대기 중")
    private String status;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "관리자 답변", example = "제보해주셔서 감사합니다. 검토 후 조치하겠습니다.")
    private String adminReply;

    @Schema(description = "생성 시간", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2025-01-20T10:30:00")
    private LocalDateTime updatedAt;

    public ReportResponseDto() {}

    public ReportResponseDto(Long id, Long placeId, String content, String author, 
                         String status, String imageUrl, String adminReply,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.placeId = placeId;
        this.content = content;
        this.author = author;
        this.status = status;
        this.imageUrl = imageUrl;
        this.adminReply = adminReply;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getAdminReply() {
        return adminReply;
    }

    public void setAdminReply(String adminReply) {
        this.adminReply = adminReply;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
