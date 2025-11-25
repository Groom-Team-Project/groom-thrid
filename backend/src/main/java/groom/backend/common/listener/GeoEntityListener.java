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

    // EPSG가 이미 5181이면 스킵
    if (geo.getEPSG() != null && geo.getEPSG() == "EPSG:5181") return;

    if (geo.getLat() == null || geo.getLng() == null) return;

    // 좌표 변환 수행
    Float[] converted = GeoTransformUtils.toEPSG5186(
            geo.getLat(),
            geo.getLng(),
            geo.getEPSG()
    );

    geo.setLat(converted[0]);
    geo.setLng(converted[1]);
    geo.setEPSG("EPSG:5181");
  }
}
