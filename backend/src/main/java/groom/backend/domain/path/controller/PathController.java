package groom.backend.domain.path.controller;

import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;
import groom.backend.domain.path.service.impl.PathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/paths")
public class PathController {
  private final PathService pathService;

  @GetMapping
  @Operation(
          summary = "장애물 호피 길찾기 기능",
          description = "현재 위치 기반 종료지점까지 계단과 같은 장애물을 회피하는 경로를 생성합니다."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
  })
  public PathFindResponse pathFind(@Valid @RequestBody PathFindRequest pathFindRequest) {
    // 입력 DTO 유효성 검사 (필드에서 검증)

    return pathService.findPath(pathFindRequest);
  }

}
