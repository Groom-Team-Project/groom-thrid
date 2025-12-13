package groom.backend.domain.opendata.service.impl;

import groom.backend.domain.opendata.dto.request.NearbyRequest;
import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ChargerLocationResponse;
import groom.backend.domain.opendata.repository.spec.ChargerLocationRepository;
import groom.backend.domain.opendata.service.spec.ChargerLocationFindService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.BoundingBox;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChargerLocationFindServiceImpl implements ChargerLocationFindService {

  private final ChargerLocationRepository repository;
  private final RedisTemplate<String, Object> redisTemplate;
  @Cacheable(value = "chargers",
          key = "#id",
          cacheManager = "chargerCacheManager")
  @Override
  public ChargerLocationResponse getChargerLocationById(Long id) {
    return repository.findById(id);
  }
}
