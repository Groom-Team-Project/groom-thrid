package groom.backend.domain.opendata.service.spec;

import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ConvenientFacilityResponse;
import groom.backend.domain.opendata.enums.FacilityType;

import java.util.List;

public interface ConvenientFacilityService {

    void getAllOpenDataConvenientFacilities();

    ConvenientFacilityResponse getConvenientFacilityById(String id);

    ConvenientFacilityResponse updateConvenientFacilityInfo(String id);

    List<ConvenientFacilityResponse> getConvenientFacilityByViewport(FacilityType facilityType, ViewportRequest viewportRequest);
}
