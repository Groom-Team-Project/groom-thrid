package groom.backend.interfaces.kakao.dto.response;

import lombok.*;

/**
 * 지번 주소 및 도로 주소를 담는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class KakaoAddress {
  private KakaoRoadAddress roadAddress;   // 도로명 주소 VO
  private KakaoLotAddress address;         // 지번 주소 VO
}