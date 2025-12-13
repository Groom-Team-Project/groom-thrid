package groom.backend.domain.opendata.service.spec;

import groom.backend.domain.opendata.dto.request.ViewportRequest;
import groom.backend.domain.opendata.dto.response.ChargerLocationResponse;

import java.util.List;

public interface ChargerLocationFindService {
  ChargerLocationResponse getChargerLocationById(Long id);

}
