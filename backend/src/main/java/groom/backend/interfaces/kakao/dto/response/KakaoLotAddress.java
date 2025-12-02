package groom.backend.interfaces.kakao.dto.response;

import lombok.*;

/**
 * 카카오 API 요청 시 응답하는 지번 주소지 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KakaoLotAddress {
  private String addressName;
  private String region1DepthName;
  private String region2DepthName;
  private String region3DepthName;
  private String mountainYn;
  private String mainAddressNo;
  private String subAddressNo;
}
