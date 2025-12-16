package groom.backend.domain.opendata.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OpenDataCharger {
    // 시설명
    private String fcltyNm;
    // 시도명
    private String ctprvnNm;
    // 시군구명
    private String signguNm;
    // 시군구 코드
    private Integer signguCode;
    // 소재지도로명주소
    private String rdnmadr;
    // 소재지지번주소
    private String lnmadr;
    // 위도
    private Double latitude;
    // 경도
    private Double longitude;
    // 설치 장소 설명
    private String instlLcDesc;
    // 평일운영시작시각
    private LocalTime weekdayOperOpenHhmm;
    // 평일운영종료시각
    private LocalTime weekdayOperColseHhmm;
    // 토요일운영시작시각
    private LocalTime satOperOperOpenHhmm;
    // 토요일운영종료시각
    private LocalTime satOperCloseHhmm;
    // 공휴일운영시작시각
    private LocalTime holidayOperOpenHhmm;
    // 공휴일운영종료시각
    private LocalTime holidayCloseOpenHhmm;
    // 동시사용가능대수
    private Integer smtmUseCo;
    // 공기주입가능여부
    private String airInjectorYn;
    // 휴대전화충전가능여부
    private String moblphonChrstnYn;
    // 관리기관명
    private String institutionNm;
    // 관리기관전화번호
    private String institutionPhoneNumber;
    // 데이터기준일자
    private LocalDate referenceDate;
    // 제공기관코드
    private String insttCode;
    // 제공기관명
    private String insttNm;
}
