package groom.backend.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(
    description = "제보 수정 요청 DTO",
    example = "{\"content\": \"수정된 제보 내용입니다!\", \"imageUrl\": \"https://example.com/image2.jpg\"}"
)
public record UpdateReportRequest(
    @Schema(description = "제보 내용", example = "수정된 제보 내용입니다!")
    @NotBlank(message = "제보 내용은 필수입니다")
    @Size(max = 2000, message = "제보 내용은 2000자 이하여야 합니다")
    String content,

    @Schema(description = "이미지 URL", example = "https://example.com/image2.jpg")
    @Size(max = 30000000, message = "이미지 URL은 30000000자 이하여야 합니다 (약 20MB 이미지)")
    String imageUrl
) {
}


