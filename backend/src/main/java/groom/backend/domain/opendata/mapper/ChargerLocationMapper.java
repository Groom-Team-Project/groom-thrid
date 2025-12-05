package groom.backend.domain.opendata.mapper;

import groom.backend.domain.opendata.dto.OpenDataCharger;
import groom.backend.domain.opendata.dto.response.ChargerLocationResponse;
import groom.backend.domain.opendata.entity.ChargerLocation;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ChargerLocationMapper {

    public static List<ChargerLocation> toEntityList(List<OpenDataCharger> dtoList) {
        return dtoList.stream()
                .map(dto -> ChargerLocation.builder()
                        .facilityName(dto.getFcltyNm())
                        .cityName(dto.getCtprvnNm())
                        .districtName(dto.getSignguNm())
                        .districtCode(dto.getSignguCode())
                        .roadAddr(dto.getRdnmadr())
                        .landAddr(dto.getLnmadr())
                        .lat(dto.getLatitude())
                        .lng(dto.getLongitude())
                        .description(dto.getInstlLcDesc())
                        .weekdayStart(dto.getWeekdayOperOpenHhmm())
                        .weekdayEnd(dto.getWeekdayOperColseHhmm())
                        .saturdayStart(dto.getSatOperOperOpenHhmm())
                        .saturdayEnd(dto.getSatOperCloseHhmm())
                        .holidayStart(dto.getHolidayOperOpenHhmm())
                        .holidayEnd(dto.getHolidayCloseOpenHhmm())
                        .capacity(dto.getSmtmUseCo())
                        .isAirPump("Y".equals(dto.getAirInjectorYn()))
                        .isCharger("Y".equals(dto.getMoblphonChrstnYn()))
                        .manageOrgName(dto.getInstitutionNm())
                        .manageOrgContact(dto.getInstitutionPhoneNumber())
                        .dataUpdated(dto.getReferenceDate())
                        .providerCode(dto.getInsttCode())
                        .providerName(dto.getInsttNm())
                        .crs("EPSG:4326")
                        .build())
                .toList();
    }

    public static List<ChargerLocationResponse> toReponseList(List<ChargerLocation> entityList) {
        return entityList.stream()
                .map(entity -> toResponse(entity))
                .toList();
    }

    public static ChargerLocationResponse toResponse(ChargerLocation entity) {
        return ChargerLocationResponse.builder()
                .placeId(entity.getPlaceId())
                .facilityName(entity.getFacilityName())
                .cityName(entity.getCityName())
                .districtName(entity.getDistrictName())
                .districtCode(entity.getDistrictCode())
                .roadAddr(entity.getRoadAddr())
                .landAddr(entity.getLandAddr())
                .lat(entity.getLat())
                .lng(entity.getLng())
                .description(entity.getDescription())
                .weekdayStart(entity.getWeekdayStart())
                .weekdayEnd(entity.getWeekdayEnd())
                .saturdayStart(entity.getSaturdayStart())
                .saturdayEnd(entity.getSaturdayEnd())
                .holidayStart(entity.getHolidayStart())
                .holidayEnd(entity.getHolidayEnd())
                .capacity(entity.getCapacity())
                .isAirPump(entity.getIsAirPump())
                .isCharger(entity.getIsCharger())
                .manageOrgName(entity.getManageOrgName())
                .manageOrgContact(entity.getManageOrgContact())
                .dataUpdated(entity.getDataUpdated())
                .providerCode(entity.getProviderCode())
                .providerName(entity.getProviderName())
                .build();
    }

}
