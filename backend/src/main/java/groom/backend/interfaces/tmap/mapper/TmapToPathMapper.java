package groom.backend.interfaces.tmap.mapper;

import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;
import groom.backend.domain.path.vo.PathNode;
import groom.backend.domain.path.vo.PathSummary;
import groom.backend.interfaces.tmap.dto.request.TmapPathFindRequest;
import groom.backend.interfaces.tmap.dto.response.TmapPathFindResponse;
import groom.backend.interfaces.tmap.dto.response.TmapPathGeometry;
import groom.backend.interfaces.tmap.dto.response.TmapPathNodeFeature;
import groom.backend.interfaces.tmap.dto.response.TmapPathNodeProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * 경로 찾기 API와 Tmap API DTO 호환을 위한 Data Mapper
 */
public class TmapToPathMapper {
  public static PathFindResponse toPathFindResponseDto(TmapPathFindResponse tmapPathFindResponse) {
    // 첫번째 노드에 요약정보 저장됨
    TmapPathNodeProperties firstProperties = tmapPathFindResponse.getPathNodeList().getFirst().getProperties();

    PathSummary pathSummary = new PathSummary(firstProperties.getTotalDistance(), firstProperties.getTotalDistance());
//    .forEach((pathNode) -> {pathNode.})
    List<PathNode> pathNodeList = new ArrayList<>();
    for( TmapPathNodeFeature node : tmapPathFindResponse.getPathNodeList() ) {
      TmapPathGeometry geometry = node.getGeometry(); // 위치 정보
      TmapPathNodeProperties properties = node.getProperties(); // 부가 정보

      // 좌표 추출
      List<List<Double>> coordinates = new ArrayList<>();
      String type = geometry.getType();
      if (type.equals("Point")) {
        coordinates.add((List<Double>) geometry.getCoordinates());
      }
      else if (type.equals("LineString")) {
        coordinates = (List<List<Double>>) geometry.getCoordinates();
      }

      PathNode pathNode = new PathNode(
              geometry.getType(),
              properties.getIndex(),
              coordinates,
              properties.getName(),
              properties.getDescription(),
              properties.getDistance(),
              properties.getRoadType(),
              properties.getTime(),
              properties.getCategoryRoadType(),
              (Integer) Integer.parseInt(properties.getFacilityType() != "" ?
                      properties.getFacilityType() : "11") // facility Type이 문자열로 직렬화되기 때문에 정수형변환 필요, 종료지점의 경우 empty 반환함.
      );
      pathNodeList.add(pathNode);
    }
    return new PathFindResponse(pathSummary, pathNodeList, null);
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
