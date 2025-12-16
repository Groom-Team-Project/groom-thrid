package groom.backend.domain.opendata.entity;

import groom.backend.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Entity
@Table(
        name = "convenient_facility",
        indexes = {
            @Index(name = "idx_facility_location", columnList = "lat, lng"),
        }
)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ConvenientFacility extends BaseEntity {

    @Id
    private String facilityId;
    // 관리순번
    private Long facilitySeq;
    // 설립일자
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate establishedDate;
    // 시설 위도
    private Double lat;
    // 시설 경도
    private Double lng;
    // 시설명
    private String facilityName;
    // 시설유형
    private String facilityType;
    // 시설 기본 주소
	private String roadAddr;
    // 영업 여부
	private Boolean isOperating;
    // 영업상태 구분명
    private String operationStatusName;
    // 편의 시설 정보
    private String convenientFacilityInfo;

    public void setConvenientFacilityInfo(String convenientFacilityInfo) {
        this.convenientFacilityInfo = convenientFacilityInfo;
    }
}
