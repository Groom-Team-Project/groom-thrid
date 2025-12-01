package groom.backend.domain.opendata.repository.spec;

import groom.backend.domain.opendata.dto.OpenDataCharger;
import groom.backend.domain.opendata.entity.ChargerLocation;

import java.util.List;

public interface ChargerLocationRepository {

    List<ChargerLocation> saveAll(List<OpenDataCharger> locations);

    void deleteAll();

    void batchInsert(List<ChargerLocation> items);
}
