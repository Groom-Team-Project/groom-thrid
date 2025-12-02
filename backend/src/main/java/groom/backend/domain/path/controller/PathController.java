package groom.backend.domain.path.controller;

import groom.backend.domain.path.dto.request.PathFindRequest;
import groom.backend.domain.path.dto.response.PathFindResponse;
import groom.backend.domain.path.service.impl.PathService;
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
  public PathFindResponse pathFind(@Valid @RequestBody PathFindRequest pathFindRequest) {
    // 입력 DTO 유효성 검사 (필드에서 검증)

    return pathService.findPath(pathFindRequest);
  }

}
