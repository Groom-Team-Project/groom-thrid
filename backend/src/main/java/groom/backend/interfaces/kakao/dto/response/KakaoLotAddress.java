package groom.backend.interfaces.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
  @JsonProperty("region_1depth_name")
  private String region1DepthName;
  @JsonProperty("region_2depth_name")
  private String region2DepthName;
  @JsonProperty("region_3depth_name")
  private String region3DepthName;
  private String mountainYn;
  private String mainAddressNo;
  private String subAddressNo;
}
