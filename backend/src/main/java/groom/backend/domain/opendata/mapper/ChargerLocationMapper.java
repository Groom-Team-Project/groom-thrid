package groom.backend.domain.opendata.mapper;

import groom.backend.domain.opendata.dto.OpenDataCharger;
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
                        .weekdayEnd(dto.getWeekdayOperCloseHhmm())
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

}
