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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@Tag(name = "Opendata", description = "공공데이터 관련 API")
@RequestMapping("/v1/opendata")
public class ChargerLocationController {

    private final ChargerLocationService chargerLocationService;
    private final ChargerLocationFindService chargerLocationFindService;
    private final Executor opendataExecutor;

    public ChargerLocationController(ChargerLocationService chargerLocationService,
                                     @Qualifier("opendataExecutor") Executor opendataExecutor,
                                     ChargerLocationFindService chargerLocationFindService) {
        this.chargerLocationService = chargerLocationService;
        this.opendataExecutor = opendataExecutor;
        this.chargerLocationFindService = chargerLocationFindService;
    }

    @Operation(
            summary = "충전소 데이터 수집 요청",
            description = "공공데이터 포털의 전동휠체어 충전소 정보를 비동기로 수집합니다. 데이터 수집 작업은 백그라운드에서 진행되며, 즉시 응답이 반환됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "즉시 응답 성공")
    })
    @GetMapping("/chargers")
    public ResponseEntity<ApiResponse<Void>> chargers() {
        log.info("OpenApi 충전소 데이터 수집 요청");
        CompletableFuture.runAsync(() -> {
            try {
                chargerLocationService.getAllOpenDataChargers();
                log.info("OpenApi 충전소 데이터 수집 작업 완료");
            } catch (Exception e) {
                log.error("OpenApi 충전소 데이터 수집 실패: {}", e);
            }
        }, opendataExecutor);
        ApiResponse<Void> response = ApiResponse.success(202, "데이터 수집 작업이 시작되었습니다", null);
        return ResponseEntity.accepted().body(response);
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
    @GetMapping("/chargers/all")
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
    @GetMapping("/chargers/viewport")
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
    @GetMapping("/chargers/nearby")
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
    @GetMapping("/chargers/{id}")
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
