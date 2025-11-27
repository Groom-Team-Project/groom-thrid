package groom.backend.domain.path.dto.request;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PathFindRequest {
  private Double startX;
  private Double startY;
  private Double endX;
  private Double endY;
  private String startName;
  private String endName;
}
