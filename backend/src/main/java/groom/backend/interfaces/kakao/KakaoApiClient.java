package groom.backend.interfaces.kakao;

import groom.backend.interfaces.kakao.dto.request.KakaoAddressRequest;
import groom.backend.interfaces.kakao.dto.response.KakaoAddressResponse;
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
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Service
@Slf4j
public class KakaoApiClient {
  private final String kakaoApiKey;
  private final String kakaoMapUrl;

  private final RestClient restClient;

  public KakaoApiClient(RestClient.Builder builder,
                        @Value("${api.kakao.url}") String kakaoUrl,
                        @Value("${api.kakao.rest-api-key}") String kakaoApiKey,
                        @Value("${api.kakao.map-url}") String kakaoMapUrl,
                        @Value("${api.kakao.connection-timeout}") Integer connectionTimeoutMs,
                        @Value("${api.kakao.read-timeout}") Integer readTimeoutMs,
                        @Value("${api.kakao.bulkhead-thread-limit}") Integer connLimit) {

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
            .baseUrl(kakaoUrl)
            .build();

    this.kakaoApiKey = kakaoApiKey;
    this.kakaoMapUrl = kakaoMapUrl;
  }

  public KakaoAddressResponse transferToAddress(Double lng, Double lat) {
    KakaoAddressRequest request = KakaoAddressRequest.builder()
            .x(Double.toString(lng))
            .y(Double.toString(lat))
            .build();

    KakaoAddressResponse response = restClient.get().uri(uriBuilder -> uriBuilder
                    .path("/v2/local/geo/coord2address.json")
                    .queryParam("x", request.getX())
                    .queryParam("y", request.getY())
                    .queryParam("input_coord", request.getInputCoord())
                    .build())
            .header("Authorization", "KakaoAK " + kakaoApiKey)
            .header("Content-Type", "application/json;charset=UTF-8")

            .retrieve().body(KakaoAddressResponse.class);
    log.info("kakaoApiPathFind response: {}", response.getDocuments().getFirst().getAddress().toString());
    return response;
  }

  /**
   * Kakao URL scheme을 이용한 지도 api 반환
   * @param startX lng
   * @param startY lat
   * @param endX lng
   * @param endY lat
   * @return
   */
  public String pathFindUrlScheme(Double startX, Double startY, Double endX, Double endY) {
    String url = String.format(
            "%s/link/by/walk/출발지,%f,%f/목적지,%f,%f",
            kakaoMapUrl,
            startY, startX,
            endY, endX
    );
    return url;
  }
}
