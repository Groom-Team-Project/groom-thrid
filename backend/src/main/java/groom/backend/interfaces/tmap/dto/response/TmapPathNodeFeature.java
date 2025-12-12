package groom.backend.interfaces.tmap.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;

/**
 * LineString 또는 Point Feature 규격을 지키는 노드
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TmapPathNodeFeature {
  @JsonProperty("geometry")
  private TmapPathGeometry geometry;
  @JsonProperty("properties")
  private TmapPathNodeProperties properties;
}
