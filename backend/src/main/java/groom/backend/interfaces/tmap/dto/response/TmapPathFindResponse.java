package groom.backend.interfaces.tmap.dto.response;

import groom.backend.domain.path.vo.PathNode;
import groom.backend.domain.path.vo.PathSummary;
import lombok.*;

import java.util.List;

@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TmapPathFindResponse {
  private PathSummary pathSummary;
  private List<PathNode> pathNodeList;
}
