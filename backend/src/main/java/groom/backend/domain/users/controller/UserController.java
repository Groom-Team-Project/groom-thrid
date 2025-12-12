package groom.backend.domain.users.controller;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.users.dto.request.GuardianMatchRequest;
import groom.backend.domain.users.dto.request.UpdateUserRequest;
import groom.backend.domain.users.dto.response.RelationInfoResponse;
import groom.backend.domain.users.dto.response.UserResponse;
import groom.backend.domain.users.service.spec.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/user")
@Tag(name = "User", description = "사용자 관리 API")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "전체 사용자 조회",
            description = "전체 사용자 목록을 조회합니다. ADMIN 권한이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (ADMIN 권한 필요)"
            )
    })
    @GetMapping("/")
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> findAll(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user
    ) {

        List<UserResponse> result = userService.getAllUsers();

        return result;
    }

    @Operation(
            summary = "특정 사용자 조회",
            description = "userId로 특정 사용자 정보를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @GetMapping("/{userId}")
    public UserResponse findOne(
            @Parameter(description = "사용자 ID (UUID)", required = true)
            @PathVariable("userId") UUID userId
    ) {

        UserResponse user = userService.getUserById(userId);

        return user;
    }

    @Operation(
            summary = "사용자 정보 수정",
            description = "특정 사용자의 정보를 수정합니다. ADMIN 권한이 필요합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "권한 없음 (ADMIN 권한 필요)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @PatchMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse update(
            @Parameter(description = "사용자 ID (UUID)", required = true)
            @PathVariable("userId") UUID userId,
            @Valid @RequestBody UpdateUserRequest req
    ) {

        UserResponse updateUser = userService.updateUser(userId, req);

        return updateUser;
    }

    @Operation(
            summary = "사용자 삭제",
            description = "특정 사용자를 삭제합니다. (실제로는 계정을 비활성화합니다)"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "사용자를 찾을 수 없음"
            )
    })
    @DeleteMapping("/{userId}")
    public void deleteUser(
            @Parameter(description = "사용자 ID (UUID)", required = true)
            @PathVariable("userId") UUID userId
    ) {
        userService.deleteUser(userId);
    }

    @Operation(
            summary = "보호자 매칭",
            description = "현재 로그인한 사용자와 보호자를 매칭합니다. 보호자의 이메일로 매칭 요청을 보냅니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "매칭 성공"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "보호자를 찾을 수 없음"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "이미 매칭된 보호자가 존재함"
            )
    })
    @PostMapping("/guardian")
    public void guardianMatch(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user,
            @Valid @RequestBody GuardianMatchRequest req
    ) {

        UUID userId = user.userId();
        String email = req.email();

        userService.guardianMatch(userId, email);
    }

    @Operation(
            summary = "관계 정보 조회",
            description = "현재 로그인한 사용자의 보호자 관계 정보를 조회합니다. JWT 토큰에서 relationId를 추출하여 사용합니다."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = RelationInfoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "관계 정보를 찾을 수 없음"
            )
    })
    @GetMapping("/relationInfo")
    public RelationInfoResponse relationInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal AuthUser user
    ) {
        Long relationId = user.relationId();

        return userService.relationInfo(relationId);
    }
}