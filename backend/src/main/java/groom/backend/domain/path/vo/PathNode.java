package groom.backend.domain.path.vo;

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
public class PathNode {
  private String type;
  private Integer index;
  private List<List<Double>> coordinates; // 경도(lng), 위도(lat) 순
  private String name;
  private String description;
  private Integer distance; // 거리 단위시간 미터
  private Integer roadType;
  private Integer time; // 소요시간 단위시간 초
  private Integer categoryRoadType; // 특화거리 정보
  private Integer facilityType; // 구간 시설물 유형 정보
}
