package groom.backend.interfaces.tmap;

import groom.backend.interfaces.tmap.dto.request.TmapPathFindRequest;
import groom.backend.interfaces.tmap.dto.response.TmapPathFindResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class TmapApiClient {
  private final String tmapApiKey;

  private final RestClient restClient;

  public TmapApiClient(RestClient.Builder builder,
                  @Value("${api.tmap.url}") String tmapUrl,
                  @Value("${api.tmap.api-key}") String tmapApiKey) {
    this.restClient = builder.baseUrl(tmapUrl).build();
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
