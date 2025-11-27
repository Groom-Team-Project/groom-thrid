package groom.backend.common.listener;

import groom.backend.common.entity.GeoEntity;
import groom.backend.common.utils.GeoTransformUtils;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;

public class GeoEntityListener {

  @PrePersist
  @PreUpdate
  public void beforeSave(Object entity) {

    if (!(entity instanceof GeoEntity geo)) {
      return;
    }

    // EPSG가 이미 4326이면 스킵
    if (geo.getCrs() != null && geo.getCrs() == "EPSG:4326") return;

    if (geo.getLat() == null || geo.getLng() == null) return;

    // 좌표 변환 수행
    Double[] converted = GeoTransformUtils.toEPSG4326(
            geo.getLat(),
            geo.getLng(),
            geo.getCrs()
    );

    geo.setLat(converted[0]);
    geo.setLng(converted[1]);
    geo.setCrs("EPSG:4326");
  }
}
