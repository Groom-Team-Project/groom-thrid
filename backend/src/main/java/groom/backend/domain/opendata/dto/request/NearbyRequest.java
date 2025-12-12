package groom.backend.domain.opendata.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 주변 검색 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "주변 충전소 검색 요청")
public class NearbyRequest {
    @Schema(description = "위도", example = "37.5665", required = true)
    private Double lat;

    @Schema(description = "경도", example = "126.9780", required = true)
    private Double lng;

    @Schema(description = "반경 (km)", example = "5.0", defaultValue = "5.0")
    private Double radiusKm;
}
