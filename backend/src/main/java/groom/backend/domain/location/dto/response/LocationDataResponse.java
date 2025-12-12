package groom.backend.domain.location.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SSE를 통해 보호자에게 전송되는 위치 데이터 응답 DTO
 * 프론트엔드 LocationData 타입과 일치하도록 필드명 사용
 */
@Getter
@AllArgsConstructor
public class LocationDataResponse {
    private Double lat;
    private Double lng;
    private LocalDateTime timestamp; // 프론트엔드는 "timestamp" 필드를 기대

    /**
     * LocationUpdateRequest를 LocationDataResponse로 변환
     */
    public static LocationDataResponse from(Double lat, Double lng, LocalDateTime time) {
        return new LocationDataResponse(lat, lng, time);
    }
}
