package groom.backend.interfaces.tmap.dto.request;

import lombok.*;

// tmap 요청을 위한 백엔드 측의 DTO
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TmapPathFindRequest {
  private Double startX;
  private Double startY;
  private Double endX;
  private Double endY;
  private String startName;
  private String endName;
  @Builder.Default
  private Integer searchOption = 30; // 경로 옵션 : 계단 회피
  @Builder.Default
  private String sort = "index"; // 반환 시 경로 순서대로.
}
