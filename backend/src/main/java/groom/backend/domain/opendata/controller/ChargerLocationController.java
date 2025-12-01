package groom.backend.domain.opendata.controller;

import groom.backend.common.response.ApiResponse;
import groom.backend.domain.opendata.service.spec.ChargerLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/v1/opendata")
public class ChargerLocationController {

    private final ChargerLocationService chargerLocationService;

    public ChargerLocationController(ChargerLocationService chargerLocationService) {
        this.chargerLocationService = chargerLocationService;
    }

    @GetMapping("/chargers")
    public ApiResponse chargers() {
        log.info("OpenApi 충전소 데이터 수집 요청");
        chargerLocationService.getAllOpenDataChargers();
        log.info("OpenApi 충전소 데이터 수집 완료");
        return ApiResponse.success(200, "success", null);
    }
}
