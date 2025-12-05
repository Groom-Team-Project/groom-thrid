package groom.backend.interfaces.kakao.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * 위치-주소 변환 API에서 사용하기 위한 Request DTO
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KakaoAddressRequest {
  // 경위도의 경우 경도 lng
  private String x;

  // 경위도의 경우 위도 lat
  private String y;

  // 입력값 기준 좌표계
  // 지원 좌표계: WGS84(Default), WCONGNAMUL, CONGNAMUL, WTM, TM
  @Builder.Default
  private String inputCoord = "WGS84";
}
