package groom.backend.domain.opendata.service.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ConvenientFacilityResponse;
import groom.backend.domain.opendata.enums.FacilityType;
import groom.backend.domain.opendata.mapper.ConvenientFacilityMapper;
import groom.backend.domain.opendata.repository.spec.ConvenientFacilityRepository;
import groom.backend.domain.opendata.service.spec.ConvenientFacilityService;
import groom.backend.domain.opendata.vo.OpenDataConvenientFacility;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

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
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ConvenientFacilityServiceImpl implements ConvenientFacilityService {

    @Value("${api.opendata.convenient-facility.list.url}")
    private String CONVEIENT_FACiLITY_LIST_URL;
    @Value("${api.opendata.convenient-facility.info.url}")
    private String CONVEIENT_FACiLITY_INFO_URL;
    @Value("${api.opendata.api-key}")
    private String SERVICE_KEY;
    private static final int DEFAULT_PAGE_SIZE = 1000;

    private static final String CACHE_KEY_VIEWPORT = "convenientFacility:viewport:";

    private final RedisTemplate<String, Object> redisTemplate;
    private final ConvenientFacilityRepository repository;
    private final XmlMapper xmlMapper = new XmlMapper();

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
    public void getAllOpenDataConvenientFacilities() {

        int pageNo = 1;
        int numOfRows = DEFAULT_PAGE_SIZE;
        while (true) {

            String url = buildUrl(CONVEIENT_FACiLITY_LIST_URL, pageNo, numOfRows);
            String body = fetchResponseBody(url);

            if (body == null || body.isBlank()) {
                log.warn("빈 응답 본문으로 인해 데이터 수집 중단");
                return;
            }

            List<OpenDataConvenientFacility> results = parseToList(body);
            if (results.isEmpty()) {
                break;
            }

            processPageBatch(results);

            pageNo++;

            log.info("편의시설 데이터 수집 진행 중 - 페이지: {}, 건수: {}", pageNo, results.size());
        }
    }

    private void processPageBatch(List<OpenDataConvenientFacility> results) {
        repository.batchInsert(ConvenientFacilityMapper.toEntityList(results));
    }

    private List<OpenDataConvenientFacility> parseToList(String body) {
        if (body == null || body.isBlank()) return Collections.emptyList();

        try {
            JsonNode root = xmlMapper.readTree(body.getBytes(StandardCharsets.UTF_8));
            ArrayNode arr = findFirstArrayNode(root);
            if (arr != null && arr.size() > 0) {
                return jsonMapper.readValue(arr.toString(), new TypeReference<List<OpenDataConvenientFacility>>() {});
            }
        } catch (Exception e) {
            log.error("XML 응답 파싱 중 오류 발생", e);
        }

        // 3) 못 찾으면 빈 리스트 반환
        return Collections.emptyList();
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
                    .header("Accept", "application/xml")
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

    private String buildUrl(String url ,int pageNo, int numOfRows) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?")
                .append(URLEncoder.encode("serviceKey", StandardCharsets.UTF_8))
                .append("=")
                .append(URLEncoder.encode(SERVICE_KEY, StandardCharsets.UTF_8));
        sb.append("&").append(URLEncoder.encode("pageNo", StandardCharsets.UTF_8))
                .append("=").append(URLEncoder.encode(String.valueOf(pageNo), StandardCharsets.UTF_8));
        sb.append("&").append(URLEncoder.encode("numOfRows", StandardCharsets.UTF_8))
                .append("=").append(URLEncoder.encode(String.valueOf(numOfRows), StandardCharsets.UTF_8));
        sb.append("&").append(URLEncoder.encode("faclTyCd", StandardCharsets.UTF_8))
                .append("=").append(URLEncoder.encode(String.valueOf(FacilityType.PUBLIC_TOILET.getLabel()), StandardCharsets.UTF_8));
        return sb.toString();
    }


    @Override
    public ConvenientFacilityResponse getConvenientFacilityById(Long id) {
        return repository.findById(id);
    }

    /**
     * 전략 2: Viewport 기반 충전소 조회
     * - 지도 이동 시 사용
     * - 화면에 보이는 영역만 조회
     * - 인덱스 활용으로 빠른 쿼리
     */
    @Override
    public List<ConvenientFacilityResponse> getConvenientFacilityByViewport(FacilityType facilityType, ViewportRequest viewportRequest) {
        log.info("DB에서 Viewport 기반 편의시설 조회: {}", viewportRequest.toString());

        int precision = 4; // 또는 5
        double minLat = floorToPrecision(viewportRequest.getMinLat(), precision);
        double maxLat = ceilToPrecision(viewportRequest.getMaxLat(), precision);
        double minLng = floorToPrecision(viewportRequest.getMinLng(), precision);
        double maxLng = ceilToPrecision(viewportRequest.getMaxLng(), precision);

        String cacheKey = buildViewportCacheKey(facilityType.name(), minLat, maxLat, minLng, maxLng, precision);
        @SuppressWarnings("unchecked")
        List<ConvenientFacilityResponse> cached = (List<ConvenientFacilityResponse>) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            log.info("Viewport 캐시 히트: {}개", cached.size());
            return cached;
        }

        ViewportRequest normalized = new ViewportRequest(minLat, maxLat, minLng, maxLng);
        List<ConvenientFacilityResponse> chargers = repository.findByLatBetweenAndLngBetween(facilityType, normalized);

        // 5분간 캐싱
        redisTemplate.opsForValue().set(cacheKey, chargers, Duration.ofMinutes(5));

        log.info("Viewport 조회 완료: {}개", chargers.size());
        return chargers;
    }

    // 소수점 precision으로 내림(확장 방향: 더 넓게)
    private static double floorToPrecision(double value, int precision) {
        double factor = Math.pow(10, precision);
        return Math.floor(value * factor) / factor;
    }

    // 소수점 precision으로 올림(확장 방향: 더 넓게)
    private static double ceilToPrecision(double value, int precision) {
        double factor = Math.pow(10, precision);
        return Math.ceil(value * factor) / factor;
    }

    private String buildViewportCacheKey(String category, double minLat, double maxLat, double minLng, double maxLng, int precision) {
        String fmt = "%s%s:%." + precision + "f:%." + precision + "f:%." + precision + "f:%." + precision + "f";
        return String.format(Locale.ROOT, fmt, CACHE_KEY_VIEWPORT, category, minLat, maxLat, minLng, maxLng);
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
