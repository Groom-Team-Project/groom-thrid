package groom.backend.domain.location.dto.reponse;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(
    description = "위치 생성 응답 DTO",
    example = "{\"id\": 1, \"name\": \"서울역\", \"address\": \"서울시 중구 중로 1\", \"latitude\": 37.5555, \"longitude\": 126.9721, \"createdAt\": \"2025-11-25T10:30:00\"}"
)
public class CreateLocationResponse {

    @Schema(description = "위치 ID", example = "1")
    private Long id;

    @Schema(description = "위치 이름", example = "서울역")
    private String name;

    @Schema(description = "위치 주소", example = "서울시 중구 중로 1")
    private String address;

    @Schema(description = "위도", example = "37.5555")
    private Double latitude;

    @Schema(description = "경도", example = "126.9721")
    private Double longitude;

    @Schema(description = "생성 시간", example = "2025-11-25T10:30:00")
    private LocalDateTime createdAt;

    public CreateLocationResponse() {}

    public CreateLocationResponse(Long id, String name, String address, Double latitude, Double longitude, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
