package groom.backend.domain.path.service.impl;

import groom.backend.common.redis.PathStreamer;
import groom.backend.common.security.AuthUser;
import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathNavigationResponse;
import groom.backend.domain.path.service.spec.PathNavigateService;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PathNavigateServiceImpl implements PathNavigateService {
  private final PathStreamer pathStreamer;

  @Override
  public void startNavigation(PathFindRequest pathFindRequest, AuthUser principal) {
    // principal 기반 relation 조회 ( 사용자와 보호자는 1:1 연관관계임이 보장됨. )
    Long relationId = principal.relationId();
    log.info("길안내 시작. userId={}, relationId={}, startName={}, endName={}",
            principal.userId(), relationId, pathFindRequest.getStartName(), pathFindRequest.getEndName());

    pathStreamer.publish(relationId, pathFindRequest);
    log.info("Redis Stream에 경로 정보 발행 완료. relationId={}", relationId);
  }

  @Override
  public void endNavigation(AuthUser principal) {
    Long relationId = principal.relationId();
    log.info("길안내 종료. userId={}, relationId={}", principal.userId(), relationId);

    pathStreamer.delete(relationId);
    log.info("Redis Stream에서 경로 정보 삭제 완료. relationId={}", relationId);
  }

  @Override
  public PathNavigationResponse getCurrentNavigation(AuthUser principal) {
    Long relationId = principal.relationId();

    if (relationId == null) {
      log.warn("RelationId가 null입니다. userId={}", principal.userId());
      return new PathNavigationResponse(null, null, null, null, null, null, false);
    }

    log.info("경로 정보 조회 시작. relationId={}", relationId);

    // Redis Stream에서 경로 정보 읽기
    Optional<Map<Object, Object>> streamData = pathStreamer.read(relationId);

    if (streamData.isPresent()) {
      Map<Object, Object> data = streamData.get();
      log.info("Redis Stream 데이터 조회 성공. relationId={}, data={}", relationId, data);

      // Redis Stream 데이터를 DTO로 변환
      PathNavigationResponse response = new PathNavigationResponse(
              String.valueOf(data.get("startX")),
              String.valueOf(data.get("startY")),
              String.valueOf(data.get("startName")),
              String.valueOf(data.get("endX")),
              String.valueOf(data.get("endY")),
              String.valueOf(data.get("endName")),
              true // 길안내 중
      );

      log.info("경로 정보 변환 완료. response={}", response);
      return response;
    } else {
      // 길안내 정보가 없는 경우 (길안내 중이 아님)
      log.info("Redis Stream에 경로 정보 없음. relationId={}", relationId);
      return new PathNavigationResponse(null, null, null, null, null, null, false);
    }
  }
}
