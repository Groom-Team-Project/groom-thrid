package groom.backend.interfaces.kakao.vo;

import lombok.*;

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
