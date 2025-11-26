package groom.backend.domain.location.service.impl;

import groom.backend.domain.location.dto.request.CreateLocationRequest;
import groom.backend.domain.location.dto.reponse.CreateLocationResponse;
import groom.backend.domain.location.service.spec.LocationService;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl implements LocationService {

    @Override
    public CreateLocationResponse createLocation(CreateLocationRequest request) {
        // TODO: Repository를 통해 위치 저장
        // Location location = Location.create(request.name(), request.address(), request.latitude(), request.longitude());
        // locationRepository.save(location);
        // return LocationMapper.toDto(location);
        return null;
    }

    @Override
    public CreateLocationResponse getLocation(Long locationId) {
        // TODO: Repository를 통해 위치 조회
        // Location location = locationRepository.findById(locationId)
        //     .orElseThrow(() -> new LocationNotFoundException("위치를 찾을 수 없습니다"));
        // return LocationMapper.toDto(location);
        return null;
    }
}
