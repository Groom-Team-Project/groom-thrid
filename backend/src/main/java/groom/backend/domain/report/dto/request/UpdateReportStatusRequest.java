package groom.backend.domain.report.dto.request;

import groom.backend.domain.report.entity.ReportStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(
    description = "관리자 제보 상태 변경 요청 DTO",
    example = "{\"status\": \"APPROVED\", \"adminReply\": \"제보해주셔서 감사합니다. 검토 후 조치하겠습니다.\"}"
)
public record UpdateReportStatusRequest(
    @Schema(description = "제보 상태 (PROCESSING, APPROVED, REJECTED)", example = "APPROVED")
    @NotNull(message = "상태는 필수입니다")
    ReportStatus status,

    @Schema(description = "관리자 답변 (승인/반려 시 사용자에게 전달)", example = "제보해주셔서 감사합니다. 검토 후 조치하겠습니다.")
    @Size(max = 2000, message = "답변은 2000자 이하여야 합니다")
    String adminReply
) {
}
