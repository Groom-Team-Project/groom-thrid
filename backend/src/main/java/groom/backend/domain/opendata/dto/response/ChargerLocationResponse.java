package groom.backend.domain.opendata.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 충전소 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "충전소 정보")
public class ChargerLocationResponse {

    @Schema(description = "충전소 ID", example = "1")
    private Long placeId;

    @Schema(description = "시설명", example = "서울역 충전소")
    private String facilityName;

    @Schema(description = "시도명", example = "서울특별시")
    private String cityName;

    @Schema(description = "시군구명", example = "용산구")
    private String districtName;

    @Schema(description = "시군구 코드", example = "11170")
    private Integer districtCode;

    @Schema(description = "소재지도로명주소", example = "서울특별시 용산구 한강대로 405")
    private String roadAddr;

    @Schema(description = "소재지지번주소", example = "")
    private String landAddr;

    @Schema(description = "위도", example = "37.55483769")
    private Double lat;

    @Schema(description = "경도", example = "126.9717341")
    private Double lng;

    @Schema(description = "설치 장소 설명", example = "2층 맞이방")
    private String description;

    @Schema(description = "평일운영시작시각", example = "06:00")
    private LocalTime weekdayStart;

    @Schema(description = "평일운영종료시각", example = "23:30")
    private LocalTime weekdayEnd;

    @Schema(description = "토요일운영시작시각", example = "06:00")
    private LocalTime saturdayStart;

    @Schema(description = "토요일운영종료시각", example = "23:30")
    private LocalTime saturdayEnd;

    @Schema(description = "공휴일운영시작시각", example = "06:00")
    private LocalTime holidayStart;

    @Schema(description = "공휴일운영종료시각", example = "23:30")
    private LocalTime holidayEnd;

    @Schema(description = "동시 사용 가능수", example = "2")
    private Integer capacity;

    @Schema(description = "공기주입가능여부", example = "N")
    private Boolean isAirPump;

    @Schema(description = "휴대전화충전가능여부", example = "N")
    private Boolean isCharger;

    @Schema(description = "관리기관명", example = "서울특별시 용산구청")
    private String manageOrgName;

    @Schema(description = "관리기관전화번호", example = "02-2199-7103")
    private String manageOrgContact;

    @Schema(description = "데이터기준일자", example = "2025-06-19")
    private LocalDate dataUpdated;

    @Schema(description = "제공기관코드", example = "3020000")
    private String providerCode;

    @Schema(description = "제공기관명", example = "서울특별시 용산구")
    private String providerName;
}
