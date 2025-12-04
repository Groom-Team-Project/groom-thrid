package groom.backend.domain.report.controller;

import groom.backend.domain.report.dto.request.CreateReportRequest;
import groom.backend.domain.report.dto.request.DeleteReportsRequest;
import groom.backend.domain.report.dto.request.UpdateReportRequest;
import groom.backend.domain.report.dto.response.ReportResponse;
import groom.backend.domain.report.service.spec.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
    public ReportResponse createReport(
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
    public List<ReportResponse> getMyReports(@RequestParam String author) {
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
    public ReportResponse getMyReport(
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
    public ReportResponse updateMyReport(
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
}

