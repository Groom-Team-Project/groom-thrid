package groom.backend.domain.opendata.controller;

import groom.backend.common.response.ApiResponse;
import groom.backend.domain.opendata.dto.response.FacilityTypeResponse;
import groom.backend.domain.opendata.enums.FacilityType;
import groom.backend.domain.opendata.service.spec.ChargerLocationService;
import groom.backend.domain.opendata.service.spec.ConvenientFacilityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Tag(name = "Opendata", description = "공공데이터 관련 API")
@RequestMapping("/v1/opendata")
public class OpendataController {

    private final ChargerLocationService chargerLocationService;
    private final ConvenientFacilityService convenientFacilityService;
    private final Executor opendataExecutor;

    public OpendataController(ChargerLocationService chargerLocationService, ConvenientFacilityService convenientFacilityService,@Qualifier("opendataExecutor") Executor opendataExecutor) {
        this.chargerLocationService = chargerLocationService;
        this.convenientFacilityService = convenientFacilityService;
        this.opendataExecutor = opendataExecutor;
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

    @Operation(
            summary = "편의시설 데이터 수집 요청",
            description = "공공데이터 포털의 장애인 편의시설 데이터를 비동기로 수집합니다. 데이터 수집 작업은 백그라운드에서 진행되며, 즉시 응답이 반환됩니다."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "202", description = "즉시 응답 성공")
    })
    @GetMapping("/convenientFacilities")
    public ResponseEntity<ApiResponse<Void>> convenientFacilities() {
        log.info("OpenApi 장애인 편의시설 데이터 수집 요청");
        CompletableFuture.runAsync(() -> {
            try {
                convenientFacilityService.getAllOpenDataConvenientFacilities();
                log.info("OpenApi 장애인 편의시설 데이터 수집 작업 완료");
            } catch (Exception e) {
                log.error("OpenApi 장애인 편의시설 데이터 수집 실패: {}", e);
            }
        }, opendataExecutor);
        ApiResponse<Void> response = ApiResponse.success(202, "데이터 수집 작업이 시작되었습니다", null);
        return ResponseEntity.accepted().body(response);
    }


    @GetMapping("/facilityTypes")
    public List<FacilityTypeResponse> getActiveFacilityTypes() {
        return Arrays.stream(FacilityType.values())
                .filter(FacilityType::isActive)
                .map(ft -> new FacilityTypeResponse(ft.name(), ft.getLabel()))
                .collect(Collectors.toList());
    }
}
