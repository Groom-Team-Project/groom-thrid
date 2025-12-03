package groom.backend.domain.path.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * LineString 또는 Point 타입의 Feature를 저장
 * TMAP API version 1에 맞춰 properties가 들어감.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "경로를 표현하는 노드 상세 정보")
public class PathNode {
  @Schema(description = "지점(Point) 또는 길(LineString)")
  private String type;

  @Schema(description = "노드 번호, Tmap API에 의한 순서 정렬이 보장됨.")
  private Integer index;

  @Schema(description = "경도(lng), 위도(lat) 순 List<Double>을 가지는 좌표 정보")
  private List<List<Double>> coordinates;

  @Schema(description = "해당 위치 이름")
  private String name;

  @Schema(description = "해당 위치 설명")
  private String description;

  @Schema(description = "현재 노드의 거리, 단위 시간 : m")
  private Integer distance;

  @Schema(description =
          """
          현재 노드의 도로 타입
          
          구분
          21: 보행자도로 1 (차도와 인도가 분리되어 있으며, 정해진 횡단구역으로만 횡단 가능한 보행자 도로)
          22: 보행자도로 2 (차도와 인도가 분리되어 있지 않거나, 보행자 횡단에 제약이 없는 보행자 도로)
          23: 보행자도로 3 (차량 통행이 불가능한 보행자도로)
          24: 보행자도로 4 (쾌적하지 않은 도로)
          """)
  private Integer roadType;

  @Schema(description = "해당 노드 소요 시간, 단위 시간 : 초")
  private Integer time;

  @Schema(description =
          """
          특화거리 정보
          
          구분
          0: 미분류
          1: 특화거리
          2: 테마거리
          3: 청소년출입금지
          """)
  private Integer categoryRoadType;

  @Schema(description =
          """
          구간 시설물 유형 정보
          
          구분
          1: 교량
          2: 터널
          3: 고가도로
          11: 일반보행자도로
          12: 육교
          14: 지하보도
          15: 횡단보도
          16: 대형시설물이동통로
          17: 계단
          """)
  private Integer facilityType; //
}
