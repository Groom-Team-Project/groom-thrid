package groom.backend.domain.review.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(
    description = "리뷰 응답 DTO",
    example = "{\"id\": 1, \"placeId\": 1, \"content\": \"좋은 장소입니다!\", \"rating\": 4.5, \"author\": \"홍길동\", \"imageUrl\": \"https://example.com/image.jpg\", \"isActive\": true, \"createdAt\": \"2025-01-20T10:30:00\", \"updatedAt\": \"2025-01-20T10:30:00\"}"
)
public class ReviewResponse {

    @Schema(description = "리뷰 ID", example = "1")
    private Long id;

    @Schema(description = "장소 ID", example = "1")
    private Long placeId;

    @Schema(description = "리뷰 내용", example = "좋은 장소입니다!")
    private String content;

    @Schema(description = "평점", example = "4.5")
    private Double rating;

    @Schema(description = "작성자", example = "홍길동")
    private String author;

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "활성화 여부", example = "true")
    private Boolean isActive;

    @Schema(description = "생성 시간", example = "2025-01-20T10:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "수정 시간", example = "2025-01-20T10:30:00")
    private LocalDateTime updatedAt;

    public ReviewResponse() {}

    public ReviewResponse(Long id, Long placeId, String content, Double rating, String author, 
                         String imageUrl, Boolean isActive, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.placeId = placeId;
        this.content = content;
        this.rating = rating;
        this.author = author;
        this.imageUrl = imageUrl;
        this.isActive = isActive;
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

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
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

