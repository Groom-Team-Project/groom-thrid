package groom.backend.domain.location.controller;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.location.dto.request.LocationUpdateRequest;
import groom.backend.domain.location.service.impl.LocationServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/locations")
@RequiredArgsConstructor
@Slf4j
public class LocationController {

    private final LocationServiceImpl locationService;

    // 위치 정보 업데이트
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLocation(
            @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody LocationUpdateRequest request) {

        log.info("위치 정보 업데이트 요청: userId={}, lat={}, lng={}, timestamp={}",
                user.relationId(), request.lat(), request.lng(), request.time());

        locationService.updateLocation(
                user.relationId(),
                request
        );
    }
}
