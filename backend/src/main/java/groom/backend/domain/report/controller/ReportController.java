package groom.backend.domain.report.controller;

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
            @Valid @RequestBody CreateReportRequest request) {
        return reportService.createReport(placeId, request);
    }

    @GetMapping("/my")
    @Operation(
            summary = "나의 제보 목록 조회",
            description = "로그인한 사용자의 제보 목록을 조회합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    public List<ReportResponseDto> getMyReports(@RequestParam String author) {
        return reportService.getMyReports(author);
    }

    @GetMapping("/my/{reportId}")
    @Operation(
            summary = "나의 제보 상세 조회",
            description = "나의 제보 상세 정보를 조회합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ReportResponseDto getMyReport(
            @PathVariable Long reportId,
            @RequestParam String author) {
        return reportService.getMyReport(reportId, author);
    }

    @PutMapping("/my/{reportId}")
    @Operation(
            summary = "나의 제보 수정",
            description = "나의 제보를 수정합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청")
    })
    public ReportResponseDto updateMyReport(
            @PathVariable Long reportId,
            @RequestParam String author,
            @Valid @RequestBody UpdateReportRequest request) {
        return reportService.updateMyReport(reportId, author, request);
    }

    @DeleteMapping("/my/{reportId}")
    @Operation(
            summary = "나의 제보 삭제",
            description = "나의 제보를 삭제합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public void deleteMyReport(
            @PathVariable Long reportId,
            @RequestParam String author) {
        reportService.deleteMyReport(reportId, author);
    }

    @DeleteMapping("/my")
    @Operation(
            summary = "나의 제보들 삭제",
            description = "나의 여러 제보를 한 번에 삭제합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public void deleteMyReports(
            @RequestParam String author,
            @Valid @RequestBody DeleteReportsRequest request) {
        reportService.deleteMyReports(request.reportIds(), author);
    }

    // ========== 관리자용 API ==========

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "모든 제보 목록 조회 (관리자)",
            description = "관리자가 모든 제보 목록을 조회합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public List<ReportResponseDto> getAllReports() {
        return reportService.getAllReports();
    }

    @PutMapping("/admin/{reportId}/status")
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

    @PutMapping("/admin/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "제보 수정 (관리자)",
            description = "관리자가 사용자 제보를 수정합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없음"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public ReportResponseDto updateReport(
            @PathVariable Long reportId,
            @Valid @RequestBody UpdateReportRequest request) {
        return reportService.updateReport(reportId, request);
    }

    @DeleteMapping("/admin/{reportId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "제보 삭제 (관리자)",
            description = "관리자가 제보를 삭제합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "제보를 찾을 수 없음"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public void deleteReport(@PathVariable Long reportId) {
        reportService.deleteReport(reportId);
    }

    @DeleteMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "제보들 일괄 삭제 (관리자)",
            description = "관리자가 여러 제보를 한 번에 삭제합니다",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청"),
            @ApiResponse(responseCode = "403", description = "권한 없음")
    })
    public void deleteReports(@Valid @RequestBody DeleteReportsRequest request) {
        reportService.deleteReports(request.reportIds());
    }
}

