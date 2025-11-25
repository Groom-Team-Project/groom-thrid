package groom.backend.common.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Entity
@MappedSuperclass
@Getter
@Setter
public class GeoEntity {
  private String EPSG;
  private Float lat;
  private Float lng;
}
