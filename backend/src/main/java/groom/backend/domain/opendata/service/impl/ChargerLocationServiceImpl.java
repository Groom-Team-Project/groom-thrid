package groom.backend.domain.opendata.service.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import groom.backend.domain.opendata.vo.OpenDataCharger;
import groom.backend.domain.opendata.dto.request.NearbyRequest;
import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ChargerLocationResponse;
import groom.backend.domain.opendata.mapper.ChargerLocationMapper;
import groom.backend.domain.opendata.repository.spec.ChargerLocationRepository;
import groom.backend.domain.opendata.service.spec.ChargerLocationFindService;
import groom.backend.domain.opendata.service.spec.ChargerLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.BoundingBox;
import org.springframework.data.redis.domain.geo.GeoLocation;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargerLocationServiceImpl implements ChargerLocationService {

    @Value("${api.opendata.charger.url}")
    private String BASE_URL;
    @Value("${api.opendata.api-key}")
    private String SERVICE_KEY;
    private static final int DEFAULT_PAGE_SIZE = 1000;

    private static final String CACHE_KEY_ALL = "chargers:all";
    private static final String CACHE_KEY_VIEWPORT = "chargers:viewport:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(30);

    private final RedisTemplate<String, Object> redisTemplate;
    private final ChargerLocationRepository repository;

    private final ChargerLocationFindService chargerLocationFindService;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final ObjectMapper jsonMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // LocalDate/LocalTime 지원
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY) // private 필드 직접 매핑 허용
            .configure(com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    @Transactional
    public void getAllOpenDataChargers() {
        repository.deleteAll();

        int totalCount = getTotalCount();
        if (totalCount <= 0) { return; }
        log.info("총 데이터 개수: {}", totalCount);

        int pageNo = 1;
        int numOfRows = DEFAULT_PAGE_SIZE;
        for (int i = 0; i <= totalCount / DEFAULT_PAGE_SIZE; i++) {

            log.info("{} 번째 API 요청 ", i + 1);
            String url = buildUrl(pageNo + i, numOfRows);
            String body = fetchResponseBody(url);

            if (body == null || body.isBlank()) {
                log.warn("빈 응답 본문으로 인해 데이터 수집 중단");
                return;
            }

            List<OpenDataCharger> results = parseToList(body);
            if (results.isEmpty()) {
                break;
            }

            processPageBatch(results);

            log.info("OpenApi 충전소 데이터 수집 진행 중: {}/{}", Math.min((i + 1) * DEFAULT_PAGE_SIZE, totalCount), totalCount);
        }
    }

    private void processPageBatch(List<OpenDataCharger> results) {
        repository.batchInsert(ChargerLocationMapper.toEntityList(results));
    }

    private List<OpenDataCharger> parseToList(String body) {
        try {
            JsonNode root = jsonMapper.readTree(body);
            ArrayNode arr = findFirstArrayNode(root);
            if (arr != null && arr.size() > 0) {
                return jsonMapper.readValue(arr.toString(), new TypeReference<List<OpenDataCharger>>() {});
            }
        } catch (Exception e) {
            log.error("API 응답 파싱 중 오류 발생", e);
        }

        // 3) 못 찾으면 빈 리스트 반환
        return Collections.emptyList();
    }

    private int getTotalCount() {
        try {
            log.info("총 데이터 개수 조회 요청");
            String url = buildUrl(1, 1);
            String body = fetchResponseBody(url);

            if (body == null || body.isBlank()) {
                // 필요시 로그 추가
                return 0;
            }

            JsonNode root = jsonMapper.readTree(body);

            return readIntAt(root, "/response/body/totalCount", 0);
        } catch (Exception e) {
            log.error("총 데이터 개수 조회 중 오류 발생", e);
        }
        return 0;
    }

    private int readIntAt(JsonNode root, String pointer, int defaultValue) {
        return root.at(pointer).asInt(defaultValue);
    }


    private ArrayNode findFirstArrayNode(JsonNode node) {
        if (node == null) return null;
        if (node.isArray()) return (ArrayNode) node;
        if (node.isObject()) {
            Iterator<JsonNode> it = node.elements();
            while (it.hasNext()) {
                JsonNode child = it.next();
                ArrayNode found = findFirstArrayNode(child);
                if (found != null) return found;
            }
        }
        return null;
    }

    private String fetchResponseBody(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> resp = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
            int status = resp.statusCode();
            if (status < 200 || status >= 300) {
                log.error("API 요청 실패: 상태 코드 {}, 응답 본문: {}", status, resp.body());
            }
            return resp.body();
        } catch (InterruptedException | IOException e) {
            log.error("API 요청 중 오류 발생", e);
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private String buildUrl(int pageNo, int numOfRows) {
        StringBuilder sb = new StringBuilder(BASE_URL);
        sb.append("?")
                .append(URLEncoder.encode("serviceKey", StandardCharsets.UTF_8))
                .append("=")
                .append(URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8));
        sb.append("&").append(URLEncoder.encode("pageNo", StandardCharsets.UTF_8))
                .append("=").append(URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8));
        sb.append("&").append(URLEncoder.encode("numOfRows", StandardCharsets.UTF_8))
                .append("=").append(URLEncoder.encode(String.valueOf(numOfRows), StandardCharsets.UTF_8));
        sb.append("&").append(URLEncoder.encode("type", StandardCharsets.UTF_8))
                .append("=").append(URLEncoder.encode("json", StandardCharsets.UTF_8));
        return sb.toString();
    }

    /**
     * 전략 1: 전체 충전소 조회 (Spring Cache + Redis)<br>
     * - 초기 로드 시 캐시 웜업을 위해 사용<br>
     * - TODO : 배치 작업으로 인한 데이터 변경 시 자동 갱신
     */
    @Override
    public List<ChargerLocationResponse> getAllChargerLocations() {
        log.info("DB에서 전체 충전소 조회 (캐시 미스)");
        List<ChargerLocationResponse> chargers = repository.findAll();

        for (ChargerLocationResponse charger : chargers) {
            redisTemplate.opsForGeo()
                    .add( "charger:geo",
                            new Point(charger.getLng(), charger.getLat()),
                            charger.getPlaceId()
                    );

        }

        log.info("총 충전소 개수: {}", chargers.size());
        return chargers;
    }

    /**
     * 전략 2: Viewport 기반 충전소 조회
     * - 지도 이동 시 사용
     * - 화면에 보이는 영역만 조회
     * - 인덱스 활용으로 빠른 쿼리
     */
    @Override
    public List<ChargerLocationResponse> getChargerLocationsByViewport(ViewportRequest viewportRequest) {
        log.info("DB에서 Viewport 기반 충전소 조회: {}", viewportRequest.toString());

        double minLat = viewportRequest.getMinLat();
        double maxLat = viewportRequest.getMaxLat();
        double minLng = viewportRequest.getMinLng();
        double maxLng = viewportRequest.getMaxLng();

        double centerLng = (minLng + maxLng) / 2;
        double centerLat = (minLat + maxLat) / 2;

        double heightKm = (maxLat - minLat) * 111.32;
        double widthKm =
                (maxLng - minLng) * 111.32 * Math.cos(Math.toRadians(centerLat));

        GeoResults<RedisGeoCommands.GeoLocation<Object>> geoResults = redisTemplate.opsForGeo()
                .search("charger:geo",
                        GeoReference.fromCoordinate(centerLng, centerLat),
                        new BoundingBox(widthKm,
                                heightKm,
                                Metrics.KILOMETERS)
                );

        return geoResults.getContent().stream()
                .map(GeoResult::getContent)  // GeoResult에서 GeoLocation 추출
                .map(GeoLocation::getName)   // placeId (long)
                .map(String::valueOf)        // string parsing
                .map(Long::parseLong)        // long parsing
                .map(chargerLocationFindService::getChargerLocationById) // to ChargerLocation (caching)
                .toList();
    }

    /**
     * 전략 3: 주변 충전소 조회 (반경 기반)
     * - 사용자 위치 기준
     * - Haversine 공식으로 거리 계산
     */
    @Override
    public List<ChargerLocationResponse> getChargerLocationsByNearby(NearbyRequest nearbyRequest) {
        log.info("주변 충전소 조회: ({}, {}) 반경 {}km", nearbyRequest.getLat(), nearbyRequest.getLng(), nearbyRequest.getRadiusKm());

        List<ChargerLocationResponse> chargers = repository.findNearbyChargers(nearbyRequest);
        log.info("주변 충전소 {}개 조회 완료", chargers.size());
        return chargers;
    }

    /**
     * 모든 캐시 삭제 (관리자용)
     */
    @CacheEvict(value = "chargers", allEntries = true)
    public void clearAllCache() {
        redisTemplate.keys(CACHE_KEY_VIEWPORT + "*")
                .forEach(redisTemplate::delete);

        log.info("모든 캐시 삭제 완료");
    }
}
