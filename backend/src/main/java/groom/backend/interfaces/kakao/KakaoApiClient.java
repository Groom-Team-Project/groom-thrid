package groom.backend.interfaces.kakao;

import groom.backend.interfaces.kakao.dto.request.KakaoAddressRequest;
import groom.backend.interfaces.kakao.dto.response.KakaoAddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
@Slf4j
public class KakaoApiClient {
  private final String kakaoApiKey;

  private final RestClient restClient;

  public KakaoApiClient(RestClient.Builder builder,
                       @Value("${api.kakao.url}") String kakaoUrl,
                       @Value("${api.kakao.rest-api-key}") String kakaoApiKey) {
    this.restClient = builder.baseUrl(kakaoUrl).build();
    this.kakaoApiKey = kakaoApiKey;
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
            .header("Authorization", "Kakao AK " + kakaoApiKey)
            .header("Content-Type", "application/json;charset=UTF-8")

            .retrieve().body(KakaoAddressResponse.class);
    return response;
  }
}
