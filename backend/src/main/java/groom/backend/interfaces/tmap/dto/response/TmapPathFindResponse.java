package groom.backend.interfaces.tmap.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapPathFindResponse {
  @JsonProperty("features")
  private List<TmapPathNodeFeature> pathNodeList;
}
