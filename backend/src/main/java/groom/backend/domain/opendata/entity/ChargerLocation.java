package groom.backend.domain.opendata.entity;

import groom.backend.common.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity(name = "charger_location")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ChargerLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long placeId;
    // 시설명
    private String facilityName;
    // 시도명
	private String cityName;
    // 시군구명
    private String districtName;
    // 시군구 코드
    private Integer districtCode;
    // 소재지도로명주소
	private String roadAddr;
    // 소재지지번주소
    private String landAddr;
    // 위도
    private Double lat;
    // 경도
    private Double lng;
    // 설치 장소 설명
    private String description;
    // 평일운영시작시각
    @DateTimeFormat(pattern = "HH:mm")
	private LocalTime weekdayStart;
    // 평일운영종료시각
    @DateTimeFormat(pattern = "HH:mm")
	private LocalTime weekdayEnd;
    // 토요일운영시작시각
    @DateTimeFormat(pattern = "HH:mm")
	private LocalTime saturdayStart;
    // 토요일운영종료시각
    @DateTimeFormat(pattern = "HH:mm")
	private LocalTime saturdayEnd;
    // 공휴일운영시작시각
    @DateTimeFormat(pattern = "HH:mm")
	private LocalTime holidayStart;
    // 공휴일운영종료시각
    @DateTimeFormat(pattern = "HH:mm")
	private LocalTime holidayEnd;
    // 동시 사용 가능수
	private Integer capacity;
    // 공기주입가능여부
	private Boolean isAirPump;
    // 휴대전화충전가능여부
    private Boolean isCharger;
    // 관리기관명
    private String manageOrgName;
    // 관리기관전화번호
	private String manageOrgContact;
    // 데이터기준일자
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dataUpdated;
    // 제공기관코드
    private String providerCode;
    // 제공기관명
    private String providerName;
    // Coordinate Reference System 지리좌표계/투영좌표계 의미
	private String crs;
}
