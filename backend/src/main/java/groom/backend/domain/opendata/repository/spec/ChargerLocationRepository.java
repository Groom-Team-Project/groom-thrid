package groom.backend.domain.opendata.repository.spec;

import groom.backend.domain.opendata.dto.request.NearbyRequest;
import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ChargerLocationResponse;
import groom.backend.domain.opendata.entity.ChargerLocation;

import java.util.List;

public interface ChargerLocationRepository {

    void deleteAll();

    void batchInsert(List<ChargerLocation> items);

    /**
     * Viewport 기반 조회 (사각형 영역)
     */
    List<ChargerLocationResponse> findByLatBetweenAndLngBetween(ViewportRequest viewportRequest);

    /**
     * 반경 기반 조회 (Haversine 공식)
     *
     * Haversine 공식:
     * distance = 2 * R * asin(sqrt(sin²((lat2-lat1)/2) + cos(lat1)*cos(lat2)*sin²((lng2-lng1)/2)))
     * R = 지구 반지름 (6371 km)
     */
    List<ChargerLocationResponse> findNearbyChargers(NearbyRequest nearbyRequest);

    List<ChargerLocationResponse> findAll();

    ChargerLocationResponse findById(Long id);
}
