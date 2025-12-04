package groom.backend.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(
    description = "제보 응답 DTO",
    example = "{\"id\": 1, \"placeId\": 1, \"content\": \"장소에 문제가 있습니다!\", \"author\": \"홍길동\", \"status\": \"대기 중\", \"imageUrl\": \"https://example.com/image.jpg\", \"createdAt\": \"2025-01-20T10:30:00\", \"updatedAt\": \"2025-01-20T10:30:00\"}"
)
public record ReportResponse(
    @Schema(description = "제보 ID", example = "1")
    Long id,

    @Schema(description = "장소 ID", example = "1")
    Long placeId,

    @Schema(description = "제보 내용", example = "장소에 문제가 있습니다!")
    String content,

    @Schema(description = "작성자", example = "홍길동")
    String author,

    @Schema(description = "제보 상태", example = "대기 중")
    String status,

    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    String imageUrl,

    @Schema(description = "생성 시간", example = "2025-01-20T10:30:00")
    LocalDateTime createdAt,

    @Schema(description = "수정 시간", example = "2025-01-20T10:30:00")
    LocalDateTime updatedAt
) {
}

