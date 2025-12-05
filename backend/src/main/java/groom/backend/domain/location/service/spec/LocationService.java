package groom.backend.domain.location.service.spec;

import groom.backend.domain.location.dto.request.CreateLocationRequest;
import groom.backend.domain.location.dto.reponse.CreateLocationResponse;

public interface LocationService {
    /**
     * 새로운 위치를 생성합니다
     */
    CreateLocationResponse createLocation(CreateLocationRequest request);

    /**
     * ID로 위치 정보를 조회합니다
     */
    CreateLocationResponse getLocation(Long locationId);
}
