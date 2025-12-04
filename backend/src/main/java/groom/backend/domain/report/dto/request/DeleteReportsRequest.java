package groom.backend.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(
    description = "제보들 삭제 요청 DTO",
    example = "{\"reportIds\": [1, 2, 3]}"
)
public record DeleteReportsRequest(
    @Schema(description = "삭제할 제보 ID 목록", example = "[1, 2, 3]")
    @NotEmpty(message = "제보 ID 목록은 필수입니다")
    List<Long> reportIds
) {
}

