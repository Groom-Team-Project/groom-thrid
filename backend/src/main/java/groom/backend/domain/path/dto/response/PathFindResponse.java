package groom.backend.domain.path.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import groom.backend.domain.path.vo.PathNode;
import groom.backend.domain.path.vo.PathSummary;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

/**
 * 길찾기 요청에 대한 응답
 * PathNode를 반환
 * null 값이 아닌 필드만 사용됨
 * 상속 및 record 사용 불가, DTO 한 개로 관리
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "길찾기 응답 DTO")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PathFindResponse {
   @Schema(description = "경로 요약 정보")
   private PathSummary pathSummary;

   @Schema(description = "경로 상세 노드 리스트")
   private List<PathNode> pathNodeList;

   @Schema(description = "대안 URL scheme, 기본 값은 null이나 경로 정보 미존재시 반드시 존재.")
   private String uri;
}
