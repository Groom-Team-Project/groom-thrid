package groom.backend.domain.opendata.service.spec;

import groom.backend.domain.opendata.dto.request.NearbyRequest;
import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ChargerLocationResponse;

import java.util.List;

public interface ChargerLocationService {

    void getAllOpenDataChargers();

    List<ChargerLocationResponse> getAllChargerLocations();

    ChargerLocationResponse getChargerLocationById(Long id);

    List<ChargerLocationResponse> getChargerLocationsByViewport(ViewportRequest viewportRequest);

    List<ChargerLocationResponse> getChargerLocationsByNearby(NearbyRequest nearbyRequest);
}
