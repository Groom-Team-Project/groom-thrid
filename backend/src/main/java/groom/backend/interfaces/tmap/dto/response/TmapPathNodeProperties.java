package groom.backend.interfaces.tmap.dto.response;

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


//                "totalDistance": 329,
//                        "totalTime": 267,
//                        "index": 0,
//                        "pointIndex": 0,
//                        "name": "",
//                        "description": "보행자도로를 따라 114m 이동",
//                        "direction": "",
//                        "nearPoiName": "",
//                        "nearPoiX": "0.0",
//                        "nearPoiY": "0.0",
//                        "intersectionName": "",
//                        "facilityType": "11",
//                        "facilityName": "",
//                        "turnType": 200,
//                        "pointType": "SP"
}
