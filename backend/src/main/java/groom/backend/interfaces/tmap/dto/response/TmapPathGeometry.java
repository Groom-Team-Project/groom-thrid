package groom.backend.interfaces.tmap.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * GeoJSON의 geometry 정보를 나타낼 시, 상속을 통해 Point, LineString을 나타낼 수 있다.
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TmapPathGeometry {
  @JsonProperty("type")
  private String type;
  @JsonProperty("coordinates")
  private Object coordinates; // List<Double>로 이루어진 좌표 값을 가짐. LineString type의 경우, List<List<Double>>을 가진다.
}
