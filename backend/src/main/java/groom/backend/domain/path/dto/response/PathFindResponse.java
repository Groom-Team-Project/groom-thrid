package groom.backend.domain.path.dto.response;

import groom.backend.domain.path.vo.PathNode;
import groom.backend.domain.path.vo.PathSummary;

import java.util.List;

public class PathFindResponse {
   private PathSummary pathSummary;
   private List<PathNode> pathNodeList;
}
