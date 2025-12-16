package groom.backend.domain.opendata.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;


@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Schema(description = "편의시설 정보")
public class ConvenientFacilityResponse {

    @Schema(description = "편의시설 Id", example = "4111710400-1-10120000")
    private String facilityId;

    @Schema(description = "관리 순번", example = "20155894527")
    private Long facilitySeq;

    @Schema(description = "설립 일자", example = "2023-01-01")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate establishedDate;

    @Schema(description = "시설 위도", example = "37.5665")
    private Double lat;

    @Schema(description = "시설 위도", example = "127.0640032")
    private Double lng;

    @Schema(description = "시설명", example = "근린공원6호 관리동")
    private String facilityName;

    @Schema(description = "시설유형", example = "공중화장실")
    private String facilityType;

    @Schema(description = "시설 기본 주소", example = "경기도 수원시 영통구 광교중앙로 216")
	private String roadAddr;

    @Schema(description = "영업상태 여부", example = "true")
	private Boolean isOperating;

    @Schema(description = "영업상태 구분명", example = "영업")
    private String operationStatusName;

    @Schema(description = "편의 시설 정보", example = "장애인사용가능화장실, 주출입구 높이차이 제거, 주출입구 접근로, 주출입구(문)")
    private String convenientFacilityInfo;
}
