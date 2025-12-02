package groom.backend.domain.path.vo;

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
public class PathSummary {
  private Integer totalDistance;
  private Integer totalTime;
}
