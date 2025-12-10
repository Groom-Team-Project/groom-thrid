package groom.backend.domain.path.service.impl;

import groom.backend.common.redis.PathStreamer;
import groom.backend.common.security.AuthUser;
import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.service.spec.PathNavigateService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PathNavigateServiceImpl implements PathNavigateService {
  private final PathStreamer pathStreamer;

  @Override
  public void startNavigation(PathFindRequest pathFindRequest, AuthUser principal) {
    // principal 기반 relation 조회 ( 사용자와 보호자는 1:1 연관관계임이 보장됨. )

    pathStreamer.publish(principal.relationId(), pathFindRequest);
  }

  @Override
  public void endNavigation(AuthUser principal) {
    pathStreamer.delete(principal.relationId());
  }
}
