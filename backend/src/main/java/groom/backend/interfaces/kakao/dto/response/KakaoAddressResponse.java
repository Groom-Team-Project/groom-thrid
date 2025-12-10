package groom.backend.interfaces.kakao.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class KakaoAddressResponse {
  @JsonProperty("documents")
  private List<KakaoAddress> documents;
}
