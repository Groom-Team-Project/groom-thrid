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
@Tag(name = "Path", description = "사용자 경로 추적 상태를 보호자에게 공유하는 API")
@RequestMapping("/v1/paths")
public class PathNavigateController {
  private final PathService pathService;

  @PostMapping("/navigation")
  @Operation(
          summary = "길안내 시작",
          description = "사용자의 위치와 도착지점을 Redis Streams를 통해 보호자에게 실시간 공유를 시작합니다."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
  })
  public PathFindResponse startNavigation(@Valid @RequestBody PathFindRequest pathFindRequest,
                                   @AuthenticationPrincipal AuthUser principal) {
    // 입력 DTO 유효성 검사 (필드에서 검증)

    // Service 레이어 호출, 사용자 길안내 추적
    return null;
  }

  @DeleteMapping("/navigation")
  @Operation(
          summary = "길안내 종료",
          description = "사용자의 길안내 상태를 종료합니다. 프론트엔드에서 수동 조작 또는 도착지 근접 시 자동 트리거됩니다."
  )
  @ApiResponses(value = {
          @ApiResponse(responseCode = "200", description = "조회 성공"),
  })
  public PathFindResponse endNavigation(@AuthenticationPrincipal AuthUser principal) {
    // 입력 DTO 유효성 검사 (필드에서 검증)

    // Service 레이어 호출, 사용자 길안내 추적 종료
    return null;
  }
}