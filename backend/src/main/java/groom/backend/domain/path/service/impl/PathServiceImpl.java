package groom.backend.domain.path.service.impl;

import groom.backend.common.exception.BusinessException;
import groom.backend.common.exception.ErrorCode;
import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathAddressResponse;
import groom.backend.domain.path.dto.response.PathFindResponse;
import groom.backend.domain.path.enums.ProvisionCity;
import groom.backend.domain.path.enums.ProvisionDistrict;
import groom.backend.domain.path.service.spec.PathService;
import groom.backend.interfaces.kakao.KakaoApiClient;
import groom.backend.interfaces.kakao.mapper.KakaoAddressMapper;
import groom.backend.interfaces.tmap.TmapApiClient;
import groom.backend.interfaces.tmap.mapper.TmapToPathMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PathServiceImpl implements PathService {
  private final TmapApiClient tmapApiClient;
  private final KakaoApiClient kakaoApiClient;

  @Override
  public PathFindResponse findPath(PathFindRequest pathFindRequest)  {
    // 서비스 제공 구역인지 검사 (시작점, 종료점 각각 검사)
    // 서비스 제공 구역 여부를 검사하는 것이므로, 두 검증에서 모두 false가 나와야 한다. 시도 단위로 제공 시 시군구 단위 정보를 제공하지 않음.
    if(!isProvisionArea(pathFindRequest.getStartX(), pathFindRequest.getStartY()) &&
            !isProvisionArea(pathFindRequest.getEndX(), pathFindRequest.getEndY()) ) {
      log.info("서비스 미제공 구역입니다.");
      throw new BusinessException(ErrorCode.PATH_SERVICE_UNAVAILABLE_AREA, (Object) new PathFindResponse(null, null,
              kakaoApiClient.pathFindUrlScheme(
                      pathFindRequest.getStartX(), pathFindRequest.getStartY(),
                      pathFindRequest.getEndX(), pathFindRequest.getEndY()
              )));
    }

    // API 호출, 값 구해오기
    PathFindResponse response = null;

    try {
      response = TmapToPathMapper.toPathFindResponseDto(
              tmapApiClient.tmapApiPathFind(
                      TmapToPathMapper.toTmapPathFindRequestDto(pathFindRequest)));
      log.info("response: {}", response);
    } catch (Exception e) {
      // 4xx 또는 5xx 에러 발생
      log.info("Exception occurred by : {}", e.toString());
      throw new BusinessException(ErrorCode.PATH_SERVICE_UNAVAILABLE_AREA, (Object) new PathFindResponse(null, null,
              kakaoApiClient.pathFindUrlScheme(
                      pathFindRequest.getStartX(), pathFindRequest.getStartY(),
                      pathFindRequest.getEndX(), pathFindRequest.getEndY()
              )));
    }

    // 반환
    return response;
  }

  /**
   * 위치 주소 변환 및 시도, 시군구 추출을 통해 서비스 제공 구역 확인
   * @param lng 경도
   * @param lat 위도
   * @return
   */
  private Boolean isProvisionArea(Double lng, Double lat) {
    // find
    PathAddressResponse address = KakaoAddressMapper.toDomain(kakaoApiClient.transferToAddress(lng, lat));

    // TODO : 시도, 시군구의 매칭 검증

    log.info(ProvisionCity.findByName(address.region1DepthName()).getName());
    log.info(ProvisionCity.findByName(address.region2DepthName()).getName());

    // check is service area
    if (ProvisionCity.findByName(address.region1DepthName()).getIsAble()
            || ProvisionDistrict.findByName(address.region2DepthName()).getIsAble()) {
      return true;
    }
    return false;
  }
}
