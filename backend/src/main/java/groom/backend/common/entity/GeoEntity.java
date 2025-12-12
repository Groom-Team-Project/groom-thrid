package groom.backend.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@MappedSuperclass
@Getter
@Setter
public class GeoEntity {
  private String crs;
  private Double lat;
  private Double lng;
}
