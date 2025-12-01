package groom.backend.interfaces.tmap.mapper;

import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;
import groom.backend.interfaces.tmap.dto.request.TmapPathFindRequest;
import groom.backend.interfaces.tmap.dto.response.TmapPathFindResponse;

public class TmapToPathMapper {
  public static PathFindResponse toPathFindResponseDto(TmapPathFindResponse tmapPathFindResponse) {
    return new PathFindResponse(tmapPathFindResponse.getPathSummary(), tmapPathFindResponse.getPathNodeList());
  }

  public static TmapPathFindRequest toTmapPathFindRequestDto(PathFindRequest PathFindRequest) {
    return TmapPathFindRequest.builder()
            .startX(PathFindRequest.getStartX())
            .startY(PathFindRequest.getStartY())
            .endX(PathFindRequest.getEndX())
            .endY(PathFindRequest.getEndY())
            .startName(PathFindRequest.getStartName())
            .endName(PathFindRequest.getEndName())
            .build();
  }
}
