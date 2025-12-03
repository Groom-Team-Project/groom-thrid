package groom.backend.domain.path.dto.response;

/**
 * KAKAO API 통신 시 도메인에서 사용하기 위한 DTO
 * 도메인과 API의 분리를 위해 샤용
 * @param addressName
 * @param region1DepthName
 * @param region2DepthName
 */
public record PathAddressResponse(String addressName,
                                  String region1DepthName,
                                  String region2DepthName) {}
