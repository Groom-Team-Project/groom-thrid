package groom.backend.domain.opendata.repository.spec.jpa;

import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.entity.ConvenientFacility;
import groom.backend.domain.opendata.enums.FacilityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface JpaConvenientFacilityRepository extends JpaRepository<ConvenientFacility, Long> {

    /**
     * Viewport 기반 조회 (사각형 영역)
     */
    @Query("SELECT c FROM ConvenientFacility c WHERE " +
            "c.lat BETWEEN :#{#viewport.minLat} AND :#{#viewport.maxLat} AND " +
            "c.lng BETWEEN :#{#viewport.minLng} AND :#{#viewport.maxLng} AND " +
            "c.isOperating IS true AND " +
            "c.facilityType = :#{#facilityType.getLabel()}" )
    List<ConvenientFacility> findByLatBetweenAndLngBetween(
            @Param("facilityType") FacilityType facilityType,
            @Param("viewport") ViewportRequest viewportRequest
    );
}
