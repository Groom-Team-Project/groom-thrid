package groom.backend.common.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;


/**
 * 사용자 위치/도착점 정보를 publish하는 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationMessageDto implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID userId;   // 위치 발신 사용자 id
  private double currentX;
  private double currentY;
  private double destX;
  private double destY;
}