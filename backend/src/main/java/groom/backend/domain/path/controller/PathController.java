package groom.backend.domain.path.controller;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;
import groom.backend.domain.path.service.spec.PathService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Path", description = "경로 찾기 API")
@RequestMapping("/v1/paths")
public class PathController {
  private final PathService pathService;

  @PostMapping
  @Operation(
          summary = "장애물 회피 길찾기 기능",
          description = "현재 위치 기반 종료지점까지 계단과 같은 장애물을 회피하는 경로를 생성합니다."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
  })
  public PathFindResponse pathFind(@Valid @RequestBody PathFindRequest pathFindRequest,
                                   @AuthenticationPrincipal AuthUser principal) {
    // 입력 DTO 유효성 검사 (필드에서 검증)

    return pathService.findPath(pathFindRequest, principal);
  }

}
