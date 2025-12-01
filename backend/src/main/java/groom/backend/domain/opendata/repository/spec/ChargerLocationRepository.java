package groom.backend.domain.opendata.repository.spec;

import groom.backend.domain.opendata.entity.ChargerLocation;

import java.util.List;

public interface ChargerLocationRepository {

    void deleteAll();

    void batchInsert(List<ChargerLocation> items);
}
