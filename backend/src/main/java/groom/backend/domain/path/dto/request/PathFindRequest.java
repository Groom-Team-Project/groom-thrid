package groom.backend.domain.path.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "경로 탐색 요청 DTO")
public class PathFindRequest {
  @Schema(description = "시작 X 좌표(경도)", example = "126.9783881", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private Double startX;

  @Schema(description = "시작 Y 좌표(위도)", example = "37.5666103", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private Double startY;

  @Schema(description = "목적지 X 좌표(경도)", example = "126.927532", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private Double endX;

  @Schema(description = "목적지 Y 좌표(위도)", example = "37.556285", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotNull
  private Double endY;

  @Schema(description = "출발지 이름", example = "서울역", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank
  private String startName;

  @Schema(description = "도착지 이름", example = "홍대입구역", requiredMode = Schema.RequiredMode.REQUIRED)
  @NotBlank
  private String endName;
}
