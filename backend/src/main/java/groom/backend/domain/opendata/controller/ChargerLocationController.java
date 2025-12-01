package groom.backend.domain.opendata.controller;

import groom.backend.common.response.ApiResponse;
import groom.backend.domain.opendata.service.spec.ChargerLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Slf4j
@RestController
@RequestMapping("/v1/opendata")
public class ChargerLocationController {

    private final ChargerLocationService chargerLocationService;
    private final Executor opendataExecutor;

    public ChargerLocationController(ChargerLocationService chargerLocationService, Executor opendataExecutor) {
        this.chargerLocationService = chargerLocationService;
        this.opendataExecutor = opendataExecutor;
    }

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
