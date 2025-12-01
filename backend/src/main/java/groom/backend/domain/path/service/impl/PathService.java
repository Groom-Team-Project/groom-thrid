package groom.backend.domain.path.service.impl;

import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;

public interface PathService {
  /**
   * 1. 서비스 제공 구역 여부 검사
   * 2. 제공 구역이 아닐 경우, 대안으로 URL scheme 반환
   * 3. API 호출 및 반환
   * @param pathFindRequest
   * @return
   */
  public PathFindResponse findPath(PathFindRequest pathFindRequest);
}
