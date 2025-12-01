package groom.backend.domain.path.service.impl;

import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;

public interface PathService {
  public PathFindResponse findPath(PathFindRequest pathFindRequest);
}
