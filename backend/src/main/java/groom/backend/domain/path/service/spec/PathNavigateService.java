package groom.backend.domain.path.service.spec;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.path.dto.request.PathFindRequest;

// TODO : 길안내 기능으로 확장
public interface PathNavigateService {
  /**
   * 사용자의 위치와 도착지점을 Redis Streams를 통해 보호자에게 실시간 공유를 시작합니다.
   */
  public void startNavigation(PathFindRequest pathFindRequest, AuthUser principal);

  /**
   * 사용자의 길안내 상태를 종료합니다.
   * 프론트엔드에서 수동 조작 또는 도착지 근접 시 자동 트리거됩니다.
   */
  public void endNavigation(AuthUser principal);
}
