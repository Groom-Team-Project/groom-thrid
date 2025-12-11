package groom.backend.domain.path.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * 보호자가 사용자의 현재 길안내 정보를 조회할 때 사용하는 응답 DTO
 */
@Getter
@AllArgsConstructor
@ToString
public class PathNavigationResponse {

    private String startX;
    private String startY;
    private String startName;
    private String endX;
    private String endY;
    private String endName;

    @JsonProperty("isNavigating") // JSON 직렬화 시 필드명 명시
    private boolean isNavigating; // 현재 길안내 중인지 여부
}
