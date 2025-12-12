package groom.backend.interfaces.tmap.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * GeoJSON의 Properties를 표현하는 DTO
 * 타입에 따라 특정 필드가 null이 될 수 있음.
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapPathNodeProperties {
  @JsonProperty("totalDistance")
  private Integer totalDistance; // 시작점 정보
  @JsonProperty("totalTime")
  private Integer totalTime; // 시작점 정보
  @JsonProperty("index")
  private Integer index; // 전체 인덱스 기준 순서. 0-index
  @JsonProperty("name")
  private String name;
  @JsonProperty("description")
  private String description;
  @JsonProperty("facilityType")
  private String facilityType; // 시설물 유형 정보. 교량, 터널, 일반보행자도로 등...
  @JsonProperty("pointType")
  private String pointType; // 시작, 경유, 종료 지점 타입. feature type이 Point일 때 존재.
  @JsonProperty("distance")
  private Integer distance;  // 거리. feature type Line
  @JsonProperty("time")
  private Integer time;  // 도달 시간(단위 : 초). feature type Line
  @JsonProperty("categoryRoadType")
  private Integer categoryRoadType;
  @JsonProperty("roadType")
  private Integer roadType; // 도로 유형 정보

}
