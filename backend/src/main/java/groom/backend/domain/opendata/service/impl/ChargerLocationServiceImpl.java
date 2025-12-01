package groom.backend.domain.opendata.service.impl;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import groom.backend.domain.opendata.dto.OpenDataCharger;
import groom.backend.domain.opendata.mapper.ChargerLocationMapper;
import groom.backend.domain.opendata.repository.spec.ChargerLocationRepository;
import groom.backend.domain.opendata.service.spec.ChargerLocationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
public class ChargerLocationServiceImpl implements ChargerLocationService {

    @Value("${api.opendata.url}")
    private String BASE_URL;
    @Value("${api.opendata.api-key}")
    private String SERVICE_KEY;
    private static final int DEFAULT_PAGE_SIZE = 1000;

    private final ChargerLocationRepository repository;

    public ChargerLocationServiceImpl(ChargerLocationRepository repository) {
        this.repository = repository;
    }

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
}
