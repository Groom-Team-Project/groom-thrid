package groom.backend.domain.opendata.repository.spec.jpa;

import groom.backend.domain.opendata.dto.request.NearbyRequest;
import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.entity.ChargerLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaChargerLocationRepository extends JpaRepository<ChargerLocation, Long> {

    /**
     * Viewport 기반 조회 (사각형 영역)
     */
    @Query("SELECT c FROM ChargerLocation c WHERE " +
            "c.lat BETWEEN :#{#viewport.minLat} AND :#{#viewport.maxLat} AND " +
            "c.lng BETWEEN :#{#viewport.minLng} AND :#{#viewport.maxLng}")
    List<ChargerLocation> findByLatBetweenAndLngBetween(
            @Param("viewport") ViewportRequest viewportRequest
    );

    /**
     * 반경 기반 조회 (Haversine 공식)
     *
     * Haversine 공식:
     * distance = 2 * R * asin(sqrt(sin²((lat2-lat1)/2) + cos(lat1)*cos(lat2)*sin²((lng2-lng1)/2)))
     * R = 지구 반지름 (6371 km)
     */
    @Query(value =
            "SELECT cl.* " +
            "FROM ( " +
                "SELECT c.*, " +
                    "(6371 * ACOS( " +
                    "  COS(RADIANS(:#{#near.lat})) * COS(RADIANS(c.lat)) * " +
                    "  COS(RADIANS(c.lng) - RADIANS(:#{#near.lng})) + " +
                    "  SIN(RADIANS(:#{#near.lat})) * SIN(RADIANS(c.lat)) ) ) AS distance " +
                "FROM charger_location c " +
                ") AS cl " +
            "WHERE distance <= :#{#near.radiusKm} " +
            "ORDER BY distance",
            nativeQuery = true
    )
    List<ChargerLocation> findNearbyChargers(@Param("near") NearbyRequest near);


}
