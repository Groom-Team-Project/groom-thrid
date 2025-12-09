package groom.backend.domain.review.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(
    description = "리뷰 생성 요청 DTO",
    example = "{\"content\": \"좋은 장소입니다!\", \"rating\": 4.5, \"imageUrl\": \"https://example.com/image.jpg\"}"
)
public record CreateReviewRequest(
    @Schema(description = "리뷰 내용", example = "좋은 장소입니다!")
    @NotBlank(message = "리뷰 내용은 필수입니다")
    @Size(max = 1000, message = "리뷰 내용은 1000자 이하여야 합니다")
    String content,

    @Schema(description = "평점 (0-5점, 0.5 단위)", example = "4.5")
    @NotNull(message = "평점은 필수입니다")
    @DecimalMin(value = "0.0", message = "평점은 0점 이상이어야 합니다")
    @DecimalMax(value = "5.0", message = "평점은 5점 이하여야 합니다")
    @RatingValidator
    Double rating,

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    @Size(max = 100000, message = "이미지 URL은 100000자 이하여야 합니다")
    String imageUrl
) {
}


