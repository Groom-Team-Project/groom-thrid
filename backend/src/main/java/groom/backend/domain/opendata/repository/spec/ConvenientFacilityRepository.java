package groom.backend.domain.opendata.repository.spec;

import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ConvenientFacilityResponse;
import groom.backend.domain.opendata.entity.ConvenientFacility;
import groom.backend.domain.opendata.enums.FacilityType;

import java.util.List;

public interface ConvenientFacilityRepository {

    void batchInsert(List<ConvenientFacility> items);

    /**
     * Viewport 기반 조회 (사각형 영역)
     */
    List<ConvenientFacilityResponse> findByLatBetweenAndLngBetween(FacilityType facilityType, ViewportRequest viewportRequest);

    ConvenientFacility findById(String id);

    void save(ConvenientFacility convenientFacility);
}
