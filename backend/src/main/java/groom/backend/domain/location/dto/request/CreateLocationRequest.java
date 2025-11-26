package groom.backend.domain.location.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(
    description = "위치 생성 요청 DTO",
    example = "{\"name\": \"서울역\", \"address\": \"서울시 중구 중로 1\", \"latitude\": 37.5555, \"longitude\": 126.9721}"
)
public record CreateLocationRequest(
    @Schema(description = "위치 이름", example = "서울역")
    @NotBlank(message = "위치 이름은 필수입니다")
    String name,

    @Schema(description = "위치 주소", example = "서울시 중구 중로 1")
    @NotBlank(message = "주소는 필수입니다")
    String address,

    @Schema(description = "위도", example = "37.5555")
    @NotNull(message = "위도는 필수입니다")
    Double latitude,

    @Schema(description = "경도", example = "126.9721")
    @NotNull(message = "경도는 필수입니다")
    Double longitude
) {
}
