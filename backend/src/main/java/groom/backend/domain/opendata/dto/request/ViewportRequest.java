package groom.backend.domain.opendata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Viewport 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "지도 영역 요청")
public class ViewportRequest {
    @Schema(description = "최소 위도", example = "37.5")
    private Double minLat;

    @Schema(description = "최대 위도", example = "37.6")
    private Double maxLat;

    @Schema(description = "최소 경도", example = "126.9")
    private Double minLng;

    @Schema(description = "최대 경도", example = "127.0")
    private Double maxLng;
}
