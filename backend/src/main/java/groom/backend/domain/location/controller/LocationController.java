package groom.backend.domain.location.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/locations")
@Tag(name = "Location", description = "위치 관리 API")
public class LocationController {

    @GetMapping("/{locationId}")
    @Operation(summary = "위치 조회", description = "ID로 위치 정보를 조회합니다")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "위치를 찾을 수 없음")
    })
    public LocationResponse getLocation(@PathVariable Long locationId) {
        return locationService.getLocation(locationId);
    }
}
