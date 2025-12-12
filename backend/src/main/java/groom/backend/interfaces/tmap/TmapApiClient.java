package groom.backend.interfaces.tmap;

import groom.backend.interfaces.tmap.dto.request.TmapPathFindRequest;
import groom.backend.interfaces.tmap.dto.response.TmapPathFindResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManagerBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class TmapApiClient {
  private final String tmapApiKey;

  private final RestClient restClient;

  public TmapApiClient(RestClient.Builder builder,
                       @Value("${api.tmap.url}") String tmapUrl,
                       @Value("${api.tmap.api-key}") String tmapApiKey,
                       @Value("${api.tmap.connection-timeout}") Integer connectionTimeoutMs,
                       @Value("${api.tmap.read-timeout}") Integer readTimeoutMs,
                       @Value("${api.tmap.bulkhead-thread-limit}") Integer connLimit) {
    // 1. 커넥션 풀 매니저(HttpClient5 전용)
    var connManager = PoolingHttpClientConnectionManagerBuilder.create()
            .setMaxConnTotal(connLimit)        // bulkhead 패턴. 전체 동시 연결 connection pool : 50
            .setMaxConnPerRoute(connLimit)     // Route별 : 50
            .build();

    // 2. Timeout 설정 (HttpClient5 스타일)
    RequestConfig requestConfig = RequestConfig.custom()
            .setConnectTimeout(Timeout.ofMilliseconds(connectionTimeoutMs))
            .setResponseTimeout(Timeout.ofMilliseconds(readTimeoutMs))
            .build();

    // 3. HttpClient 빌드
    CloseableHttpClient httpClient = HttpClients.custom()
            .setConnectionManager(connManager)
            .setDefaultRequestConfig(requestConfig)
            .build();

    // 4. RestClient 반영
    this.restClient = builder
            .requestFactory(new HttpComponentsClientHttpRequestFactory(httpClient))
            .baseUrl(tmapUrl)
            .build();

    this.tmapApiKey = tmapApiKey;
  }

  /**
   * Tmap API를 호출해 계단 회피 경로를 가져옵니다.
   * 4xx 또는 5xx 에러 발생 시 Exception을 던집니다.
   * @param pathFindRequest
   * @return
   */
  public TmapPathFindResponse tmapApiPathFind(TmapPathFindRequest pathFindRequest) {
    Integer version = 1;
    TmapPathFindResponse pathFindResponse = restClient.post().uri(uriBuilder -> uriBuilder
            .path("/tmap/routes/pedestrian")
            .queryParam("version", version).build())
            .header("appKey", tmapApiKey)
            .header("Accept", "*/*")
            .header("Content-Type", "application/json")
            .body(pathFindRequest).retrieve().body(TmapPathFindResponse.class);
    log.info("tmapApiPathFind response: {}", pathFindResponse);
    return pathFindResponse;
  }
}
