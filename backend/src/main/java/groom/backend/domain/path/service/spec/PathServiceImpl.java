package groom.backend.domain.path.service.spec;

import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;
import groom.backend.domain.path.service.impl.PathService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PathServiceImpl implements PathService {
  private final TmapApiClient tmapApiClient;

  @Override
  public PathFindResponse findPath(PathFindRequest pathFindRequest)  {
    // 서비스 제공 구역 검사

    // 제공 구역이 아닐 경우, Exception 발생

    // API 호출, 값 구해오기

    // DTO로 직렬화

    // 반환
    return null;
  }
}
