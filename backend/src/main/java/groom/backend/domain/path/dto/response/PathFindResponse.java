package groom.backend.domain.path.dto.response;

import groom.backend.domain.path.vo.PathNode;
import groom.backend.domain.path.vo.PathSummary;
import lombok.*;

import java.util.List;

/**
 * 길찾기 요청에 대한 응답
 * PathNode를 반환
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PathFindResponse {
   private PathSummary pathSummary;
   private List<PathNode> pathNodeList;
}
