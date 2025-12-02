package groom.backend.interfaces.kakao.dto.response;

import lombok.*;

/**
 * 카카오 API 요청 시 응답하는 도로 주소지 정보
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KakaoRoadAddress {
  private String addressName;
  private String region1DepthName;
  private String region2DepthName;
  private String region3DepthName;
  private String roadName;
  private String undergroundYn;
  private String mainBuildingNo;
  private String subBuildingNo;
  private String buildingName;
  private String zoneNo;

}
