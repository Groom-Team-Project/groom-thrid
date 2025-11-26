package groom.backend.domain.location.controller;

import groom.backend.domain.location.dto.request.CreateLocationRequest;
import groom.backend.domain.location.dto.reponse.CreateLocationResponse;
import groom.backend.domain.location.service.spec.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/locations")
@Tag(name = "Location", description = "위치 관리 API")
public class LocationController {

    private final LocationService locationService;

    @Autowired
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping
    @Operation(
            summary = "위치 생성",
            description = "새로운 위치 정보를 생성합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public CreateLocationResponse createLocation(@Valid @RequestBody CreateLocationRequest request) {
        return locationService.createLocation(request);
    }

    @GetMapping("/{locationId}")
    @Operation(
            summary = "위치 조회",
            description = "ID로 위치 정보를 조회합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "위치를 찾을 수 없음")
    })
    public CreateLocationResponse getLocation(@PathVariable Long locationId) {
        return locationService.getLocation(locationId);
    }

    @GetMapping("/kakao/key")
    public String getKey() {
        return "32f825b60a03b505712a82f7faefe59b";
    }
}
