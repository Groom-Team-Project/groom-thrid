package groom.backend.path;

import groom.backend.interfaces.kakao.KakaoApiClient;
import groom.backend.interfaces.kakao.dto.request.KakaoAddressRequest;
import groom.backend.interfaces.kakao.dto.response.KakaoAddressResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class KakaoCoordTransferTest {

  @Autowired
  KakaoApiClient kakaoApiClient;

  @Test
  void kakaoApiCoordSearchResponseTest() {
    // given
    KakaoAddressRequest request = KakaoAddressRequest.builder()
            .x("126.9783882")   // 서울 시청 경도
            .y("37.5666103")    // 서울 시청 위도
            .inputCoord("WGS84")
            .build();

    // when
    KakaoAddressResponse response = kakaoApiClient.transferToAddress(request);

    // then
    assertThat(response).isNotNull();
    assertThat(response.getDocuments()).isNotEmpty();

    log.info("Kakao API Response: {}", response);
    log.info("lot address", response.getDocuments().get(0).getAddress());
    log.info("road address", response.getDocuments().get(0).getRoadAddress());
  }
}
