package groom.backend.domain.opendata.mapper;

import groom.backend.domain.opendata.dto.response.ConvenientFacilityResponse;
import groom.backend.domain.opendata.entity.ConvenientFacility;
import groom.backend.domain.opendata.vo.OpenDataConvenientFacility;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Component
public class ConvenientFacilityMapper {

    public static List<ConvenientFacility> toEntityList(List<OpenDataConvenientFacility> dtoList) {
        return dtoList.stream()
                .map(dto -> ConvenientFacility.builder()
                        .facilityId(dto.getWfcltId())
                        .facilitySeq(dto.getFaclInfId())
                        .establishedDate(parseEstbDate(dto.getEstbDate()))
                        .lat(dto.getFaclLat())
                        .lng(dto.getFaclLng())
                        .facilityName(dto.getFaclNm())
                        .facilityType(dto.getFaclTyCd())
                        .roadAddr(dto.getLcMnad())
                        .isOperating("Y".equals(dto.getSalStaDivCd()))
                        .operationStatusName(dto.getSalStaNm())
                        .build())
                .toList();
    }

    public static List<ConvenientFacilityResponse> toResponseList(List<ConvenientFacility> entityList) {
        return entityList.stream()
                .map(entity -> toResponse(entity))
                .toList();
    }

    public static ConvenientFacilityResponse toResponse(ConvenientFacility entity) {
        return ConvenientFacilityResponse.builder()
                .facilityId(entity.getFacilityId())
                .facilitySeq(entity.getFacilitySeq())
                .establishedDate(entity.getEstablishedDate())
                .lat(entity.getLat())
                .lng(entity.getLng())
                .facilityName(entity.getFacilityName())
                .facilityType(entity.getFacilityType())
                .roadAddr(entity.getRoadAddr())
                .isOperating(entity.getIsOperating())
                .operationStatusName(entity.getOperationStatusName())
                .convenientFacilityInfo(entity.getConvenientFacilityInfo())
                .build();
    }

    // yyyyMMdd 또는 ISO(yyyy-MM-dd) 또는 이미 LocalDate인 값을 처리
    private static LocalDate parseEstbDate(Object raw) {
        if (raw == null) return null;
        if (raw instanceof LocalDate) {
            return (LocalDate) raw;
        }
        String s = raw.toString().trim();
        if (s.isEmpty()) return null;

        // 1) yyyyMMdd
        DateTimeFormatter yyyymmdd = DateTimeFormatter.ofPattern("yyyyMMdd");
        try {
            return LocalDate.parse(s, yyyymmdd);
        } catch (DateTimeParseException ignored) { }

        // 2) ISO yyyy-MM-dd
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException ignored) { }

        return null;
    }

}
