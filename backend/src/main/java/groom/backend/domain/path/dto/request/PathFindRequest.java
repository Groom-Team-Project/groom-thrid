package groom.backend.domain.path.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class PathFindRequest {
  @NotNull
  private Double startX;
  @NotNull
  private Double startY;
  @NotNull
  private Double endX;
  @NotNull
  private Double endY;
  @NotBlank
  private String startName;
  @NotBlank
  private String endName;
}
