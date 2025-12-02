package groom.backend.interfaces.tmap.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

/**
 * Point 타입의 geometry를 나타내는 DTO
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TmapPathPointNode implements TmapPathGeometry{
  @JsonProperty("coordinates")
  List<Double> coordinates;
}
