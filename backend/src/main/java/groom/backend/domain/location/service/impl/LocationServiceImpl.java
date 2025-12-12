package groom.backend.domain.location.service.impl;

import groom.backend.common.redis.LocationPublisher;
import groom.backend.domain.location.dto.request.LocationUpdateRequest;
import groom.backend.domain.location.service.spec.LocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {

    private final LocationPublisher locationPublisher;

    // 사용자의 위치 정보를 Redis에 발행 - 채널: location:{relationId} - 메시지: LocationData (JSON)
    @Override
    public void updateLocation(Long relationId, LocationUpdateRequest req) {
        locationPublisher.publishLocation(relationId, req);
    }
}
