package groom.backend.domain.opendata.repository.spec.jpa;

import groom.backend.domain.opendata.entity.ChargerLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaChargerLocationRepository extends JpaRepository<ChargerLocation, Long> {
}
