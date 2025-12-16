package groom.backend.domain.opendata.controller;

import groom.backend.common.response.ApiResponse;
import groom.backend.domain.opendata.dto.request.NearbyRequest;
import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ChargerLocationResponse;
import groom.backend.domain.opendata.service.spec.ChargerLocationFindService;
import groom.backend.domain.opendata.service.spec.ChargerLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "chargers", description = "충전소 위치 관련 API")
@RequestMapping("/v1/chargers")
public class ChargerLocationController {

    private final ChargerLocationService chargerLocationService;
    private final ChargerLocationFindService chargerLocationFindService;

    public ChargerLocationController(ChargerLocationService chargerLocationService,
                                     ChargerLocationFindService chargerLocationFindService) {
        this.chargerLocationService = chargerLocationService;
        this.chargerLocationFindService = chargerLocationFindService;
    }

    /**
     * 전략 1: 전체 충전소 조회 (Redis 캐싱)
     */
    @Operation(
            summary = "전체 충전소 조회",
            description = "모든 충전소 정보를 조회합니다. Redis 캐싱으로 빠른 응답을 제공합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<ChargerLocationResponse>>> getAllChargers() {
        log.info("전체 충전소 조회 요청");

        List<ChargerLocationResponse> chargers = chargerLocationService.getAllChargerLocations();

        return ResponseEntity.ok(
                ApiResponse.success(200, "조회 성공", chargers)
        );
    }

    /**
     * 전략 2: Viewport 기반 충전소 조회
     */
    @Operation(
            summary = "지도 영역 내 충전소 조회",
            description = "지도에서 보이는 영역(Viewport) 내의 충전소만 조회합니다. 지도 이동 시 사용합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/viewport")
    public ResponseEntity<ApiResponse<List<ChargerLocationResponse>>> getChargersInViewport(
            @ParameterObject @ModelAttribute ViewportRequest viewportRequest) {

        log.info("Viewport 충전소 조회 요청: lat({} ~ {}), lng({} ~ {})",
                viewportRequest.getMinLat(), viewportRequest.getMaxLat(), viewportRequest.getMinLng(), viewportRequest.getMaxLng());

        List<ChargerLocationResponse> chargers = chargerLocationService.getChargerLocationsByViewport(
                viewportRequest
        );

        return ResponseEntity.ok(
                ApiResponse.success(200, "조회 성공", chargers)
        );
    }

    /**
     * 전략 3: 주변 충전소 조회 (반경 기반)
     */
    @Operation(
            summary = "주변 충전소 조회",
            description = "현재 위치 기준 반경 내의 충전소를 조회합니다. 거리순으로 정렬됩니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/nearby")
    public ResponseEntity<ApiResponse<List<ChargerLocationResponse>>> getNearbyChargers(
            @ParameterObject @ModelAttribute NearbyRequest nearbyRequest) {

        log.info("주변 충전소 조회 요청: ({}, {}) 반경 {}km", nearbyRequest.getLat(), nearbyRequest.getLng(), nearbyRequest.getRadiusKm());

        List<ChargerLocationResponse> chargers = chargerLocationService.getChargerLocationsByNearby(nearbyRequest);

        return ResponseEntity.ok(
                ApiResponse.success(200, "조회 성공", chargers)
        );
    }

    /**
     * 충전소 상세 조회
     */
    @Operation(
            summary = "충전소 상세 조회",
            description = "특정 충전소의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ChargerLocationResponse>> getChargerById(
            @Parameter(description = "충전소 ID", example = "1", required = true)
            @PathVariable Long id) {

        log.info("충전소 상세 조회: ID={}", id);

        ChargerLocationResponse charger = chargerLocationFindService.getChargerLocationById(id);

        return ResponseEntity.ok(
                ApiResponse.success(200, "조회 성공", charger)
        );
    }
}
