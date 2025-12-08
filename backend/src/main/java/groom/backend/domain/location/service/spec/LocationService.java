package groom.backend.domain.location.service.spec;

import groom.backend.domain.location.dto.request.LocationUpdateRequest;

public interface LocationService {

    // 사용자의 위치 정보 업데이트
    void updateLocation(Long relationId, LocationUpdateRequest request);
}
