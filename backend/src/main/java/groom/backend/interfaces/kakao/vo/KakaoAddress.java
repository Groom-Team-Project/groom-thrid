package groom.backend.interfaces.kakao.vo;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KakaoAddress {
  private KakaoRoadAddress roadAddress;   // 도로명 주소 VO
  private KakaoLotAddress address;         // 지번 주소 VO
}