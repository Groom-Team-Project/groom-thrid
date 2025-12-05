package groom.backend.domain.report.controller;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.report.dto.request.CreateReportRequest;
import groom.backend.domain.report.dto.request.DeleteReportsRequest;
import groom.backend.domain.report.dto.request.UpdateReportRequest;
import groom.backend.domain.report.dto.request.UpdateReportStatusRequest;
import groom.backend.domain.report.dto.response.ReportResponseDto;
import groom.backend.domain.report.service.spec.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/v1/reports")
@Tag(name = "Report", description = "제보 관리 API")
public class ReportController {

    private final ReportService reportService;

    @Autowired
    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @PostMapping("/place/{placeId}")
    @Operation(
            summary = "제보 생성",
            description = "장소별 새로운 제보를 생성합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ReportResponseDto createReport(
            @PathVariable Long placeId,
            @Valid @RequestBody CreateReportRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        return reportService.createReport(placeId, request, authUser);
    }

    @GetMapping
    @Operation(
            summary = "제보 목록 조회",
            description = "USER, PROTECTOR: 자신이 생성한 제보 목록만 조회 / ADMIN: 모든 제보 목록 조회",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public List<ReportResponseDto> getReports(@AuthenticationPrincipal AuthUser authUser) {
        return reportService.getReports(authUser);
    }

    @GetMapping("/{reportId}")
    @Operation(
            summary = "제보 상세 조회",
            description = "USER, PROTECTOR: 자신이 생성한 제보만 조회 / ADMIN: 모든 제보 조회",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ReportResponseDto getReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal AuthUser authUser) {
        return reportService.getReport(reportId, authUser);
    }

    @PutMapping("/{reportId}")
    @Operation(
            summary = "제보 수정",
            description = "USER, PROTECTOR: 자신이 생성한 제보만 수정 / ADMIN: 모든 제보 수정",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ReportResponseDto updateReport(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        return reportService.updateReport(reportId, request, authUser);
    }

    @DeleteMapping("/{reportId}")
    @Operation(
            summary = "제보 삭제",
            description = "USER, PROTECTOR: 자신이 생성한 제보만 삭제 / ADMIN: 모든 제보 삭제",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public void deleteReport(
            @PathVariable Long reportId,
            @AuthenticationPrincipal AuthUser authUser) {
        reportService.deleteReport(reportId, authUser);
    }

    @DeleteMapping
    @Operation(
            summary = "제보들 일괄 삭제",
            description = "USER, PROTECTOR: 자신이 생성한 제보만 삭제 / ADMIN: 모든 제보 삭제",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public void deleteReports(
            @Valid @RequestBody DeleteReportsRequest request,
            @AuthenticationPrincipal AuthUser authUser) {
        reportService.deleteReports(request, authUser);
    }

    @PutMapping("/{reportId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "제보 상태 변경 (관리자)",
            description = "관리자가 제보 상태를 변경합니다. 승인 또는 반려 시 답변을 함께 전달합니다.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "상태 변경 성공"),
            @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (승인/반려 시 답변 필수)"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ReportResponseDto updateReportStatus(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportStatusRequest request) {
        return reportService.updateReportStatus(reportId, request);
    }
}


