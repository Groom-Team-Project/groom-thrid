package groom.backend.interfaces.kakao.mapper;

import groom.backend.domain.path.dto.response.PathAddressResponse;
import groom.backend.interfaces.kakao.dto.response.KakaoAddressResponse;
import groom.backend.interfaces.kakao.dto.response.KakaoAddress;

public class KakaoAddressMapper {

  private KakaoAddressMapper() {}

  public static PathAddressResponse toDomain(KakaoAddressResponse response) {

    if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
      throw new IllegalArgumentException("Kakao 응답이 비어있습니다.");
    }

    // TODO : 도로명 주소 지번 주소 시도 및 시군구 차이있는지 확인할 것
    KakaoAddress lotAddr = response.getDocuments().get(0);
    KakaoAddress roadAddr = response.getDocuments().get(0);

    // 도로명 주소 우선 사용, 없으면 지번 주소
    String fullAddress =
            roadAddr.getRoadAddress() != null
                    ? roadAddr.getRoadAddress().getAddressName()
                    : lotAddr.getAddress().getAddressName();

    String depth1 =
            roadAddr.getRoadAddress() != null
                    ? roadAddr.getRoadAddress().getRegion1DepthName()
                    : lotAddr.getAddress().getRegion1DepthName();

    String depth2 =
            roadAddr.getRoadAddress() != null
                    ? roadAddr.getRoadAddress().getRegion2DepthName()
                    : lotAddr.getAddress().getRegion2DepthName();

    return new PathAddressResponse(
            fullAddress,
            depth1,
            depth2);
  }
}
