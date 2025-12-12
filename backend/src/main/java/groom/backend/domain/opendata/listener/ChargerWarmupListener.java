package groom.backend.domain.opendata.listener;

import groom.backend.domain.opendata.dto.response.ChargerLocationResponse;
import groom.backend.domain.opendata.service.spec.ChargerLocationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ChargerWarmupListener {
  private final ChargerLocationService chargerLocationService;

  @EventListener
  public void listen(ApplicationReadyEvent event) {

    List<Long> ids = chargerLocationService.getAllChargerLocations().stream()
            .map(ChargerLocationResponse::getPlaceId)
            .toList();
    log.info("총 충전소 개수: {}", ids.size());
    ids.stream()
            .map(chargerLocationService::getChargerLocationById) // 여기서 캐시 활용
            .toList();
  }
}
