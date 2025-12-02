package groom.backend.interfaces.tmap.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * GeoJSON 기반 경로 리스트 반환 DTO
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TmapPathFindResponse {
  @JsonProperty("features")
  private List<TmapPathNodeFeature> pathNodeList;
}
