package groom.backend.interfaces;

import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;
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

  public PathFindResponse tmapApiPathFind(PathFindRequest pathFindRequest) {
    Integer version = 1;
    log.info(this.tmapApiKey);
    PathFindResponse pathFindResponse = restClient.post().uri(uriBuilder -> uriBuilder
            .path("/tmap/routes/pedestrian")
            .queryParam("version", version).build())
            .header("appKey", tmapApiKey)
            .header("Accept", "*/*")
            .header("Content-Type", "application/json")
            .body(pathFindRequest).retrieve().body(PathFindResponse.class);

    return pathFindResponse;
  }
}
