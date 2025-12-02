package groom.backend.domain.path.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 경로 요약 정보
 * totalDistance : 거리, 단위길이 미터
 * totalTime : 소요시간, 단위시간 분
 * 시작점, 종료점
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "경로 요약 정보")
public class PathSummary {
  @Schema(description = "전체 거리(m)", example = "532")
  private Integer totalDistance;
  @Schema(description = "전체 시간(초)", example = "420")
  private Integer totalTime;
}
