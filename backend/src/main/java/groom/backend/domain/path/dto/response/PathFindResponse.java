package groom.backend.domain.path.dto.response;

import groom.backend.domain.path.vo.PathNode;
import groom.backend.domain.path.vo.PathSummary;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "길찾기 응답 DTO")
public class PathFindResponse {
   @Schema(description = "경로 요약 정보")
   private PathSummary pathSummary;

   @Schema(description = "경로 상세 노드 리스트")
   private List<PathNode> pathNodeList;
}
