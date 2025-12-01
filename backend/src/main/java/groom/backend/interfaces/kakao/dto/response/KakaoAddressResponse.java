package groom.backend.interfaces.kakao.dto.response;

import groom.backend.interfaces.kakao.vo.KakaoAddress;
import groom.backend.interfaces.kakao.vo.KakaoLotAddress;
import lombok.*;

import java.util.List;

/**
 * 위치-주소 변환 API에서 사용하기 위한 Response DTO
 */
@Setter
@Getter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class KakaoAddressResponse {
  private List<KakaoAddress> documents;
}
