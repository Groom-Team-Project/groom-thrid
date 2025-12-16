package groom.backend.domain.opendata.controller;

import groom.backend.common.response.ApiResponse;
import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ConvenientFacilityResponse;
import groom.backend.domain.opendata.enums.FacilityType;
import groom.backend.domain.opendata.service.spec.ConvenientFacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@Tag(name = "ConvenientFacilities", description = "장애인 편의시설 관련 API")
@RequestMapping("/v1/ConvenientFacilities")
public class ConvenientFacilityLocationController {

    private final ConvenientFacilityService convenientFacilityService;
    private final Executor opendataExecutor;


    public ConvenientFacilityLocationController(ConvenientFacilityService convenientFacilityService, Executor opendataExecutor) {
        this.convenientFacilityService = convenientFacilityService;
        this.opendataExecutor = opendataExecutor;
    }

    /**
     * Viewport 기반 장애인 편의시설 조회
     */
    @Operation(
            summary = "지도 영역 내 장애인 편의시설 조회",
            description = "지도에서 보이는 영역(Viewport) 내의 편의시설만 조회합니다. 지도 이동 시 사용합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            )
    })
    @GetMapping("/viewport")
    public ResponseEntity<ApiResponse<List<ConvenientFacilityResponse>>> getConvenientFacilityInViewport(
            @Parameter(description = "편의시설 유형", example = "공중화장실", required = true)
            @RequestParam("facilityType") FacilityType facilityType,
            @ParameterObject @ModelAttribute ViewportRequest viewportRequest) {

        log.info("Viewport 편의시설 조회 요청: lat({} ~ {}), lng({} ~ {})",
                viewportRequest.getMinLat(), viewportRequest.getMaxLat(), viewportRequest.getMinLng(), viewportRequest.getMaxLng());

        List<ConvenientFacilityResponse> chargers = convenientFacilityService.getConvenientFacilityByViewport(
                facilityType,
                viewportRequest
        );

        return ResponseEntity.ok(
                ApiResponse.success(200, "조회 성공", chargers)
        );
    }

    /**
     * 편의시설 상세 조회
     */
    @Operation(
            summary = "편의 시설 상세 조회",
            description = "특정 편의시설의 상세 정보를 조회합니다."
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ConvenientFacilityResponse>> getConvenientFacilityById(
            @Parameter(description = "편의시설 ID", example = "1", required = true)
            @PathVariable String id) {

        log.info("편의시설 상세 조회: ID={}", id);

        ConvenientFacilityResponse facility = convenientFacilityService.getConvenientFacilityById(id);

        return ResponseEntity.ok(
                ApiResponse.success(200, "조회 성공", facility)
        );
    }

    /**
     * 편의시설 기구목록 조회
     */
    @Operation(
            summary = "편의시설 기구목록 조회",
            description = "특정 편의시설의 편의시설 기구목록을 조회합니다."
    )
    @PostMapping("/info/{id}/refresh")
    public ResponseEntity<ApiResponse<Void>> getConvenientFacilityInfo(
            @Parameter(description = "편의시설 ID", example = "1", required = true)
            @PathVariable String id) {

        log.info("편의시설 편의시설 기구목록 조회: ID={}", id);
        CompletableFuture.runAsync(() -> {
            try {
                convenientFacilityService.updateConvenientFacilityInfo(id);
            } catch (Exception e) {
                log.error("OpenApi 장애인 편의시설 데이터 수집 실패: {}", e);
            }
        }, opendataExecutor);
        ApiResponse<Void> response = ApiResponse.success(202, "데이터 수집 작업이 시작되었습니다", null);
        return ResponseEntity.accepted().body(response);

    }
}
