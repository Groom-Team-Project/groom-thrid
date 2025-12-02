package groom.backend.domain.opendata.controller;

import groom.backend.common.response.ApiResponse;
import groom.backend.domain.opendata.service.spec.ChargerLocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@Tag(name = "Opendata", description = "공공데이터 관련 API")
@RequestMapping("/v1/opendata")
public class ChargerLocationController {

    private final ChargerLocationService chargerLocationService;
    private final Executor opendataExecutor;

    public ChargerLocationController(ChargerLocationService chargerLocationService, Executor opendataExecutor) {
        this.chargerLocationService = chargerLocationService;
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
    public ApiResponse chargers() {
        log.info("OpenApi 충전소 데이터 수집 요청");
        CompletableFuture.runAsync(() -> {
            try {
                chargerLocationService.getAllOpenDataChargers();
                log.info("OpenApi 충전소 데이터 수집 작업 완료");
            } catch (Exception e) {
                log.error("OpenApi 충전소 데이터 수집 실패: {}", e);
            }
        }, opendataExecutor);
        return ApiResponse.success(202, "success", null);
    }
}
