package groom.backend.domain.opendata.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class OpenDataConvenientFacility {
    // 시설 ID
    private String wfcltId;
    // 관리순번
    private Long faclInfId;
    // 설립일자
    private String estbDate;
    // 시설위도
    private Double faclLat;
    // 시설경도
    private Double faclLng;
    // 시설명
    private String faclNm;
    // 시설유형
    private String faclTyCd;
    // 시설 기본주소
    private String lcMnad;
    // 영업상태 구분 코드
    private String salStaDivCd;
    // 영업상태 구분명
    private String salStaNm;
    // 복지로관리 시설 구분 코드
    private String wfcltDivCd;
}
