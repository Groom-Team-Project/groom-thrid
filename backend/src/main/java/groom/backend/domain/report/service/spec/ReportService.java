package groom.backend.domain.report.service.spec;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.report.dto.request.CreateReportRequest;
import groom.backend.domain.report.dto.request.DeleteReportsRequest;
import groom.backend.domain.report.dto.request.UpdateReportRequest;
import groom.backend.domain.report.dto.request.UpdateReportStatusRequest;
import groom.backend.domain.report.dto.response.ReportResponseDto;

import java.util.List;

public interface ReportService {
    /**
     * 새로운 제보를 생성합니다
     */
    ReportResponseDto createReport(Long placeId, CreateReportRequest request, AuthUser authUser);

    /**
     * 제보 목록을 조회합니다
     * USER, PROTECTOR: 자신이 생성한 제보 목록만 조회
     * ADMIN: 모든 제보 목록 조회
     */
    List<ReportResponseDto> getReports(AuthUser authUser);

    /**
     * 제보 상세를 조회합니다
     * USER, PROTECTOR: 자신이 생성한 제보만 조회
     * ADMIN: 모든 제보 조회
     */
    ReportResponseDto getReport(Long reportId, AuthUser authUser);

    /**
     * 제보를 수정합니다
     * USER, PROTECTOR: 자신이 생성한 제보만 수정
     * ADMIN: 모든 제보 수정
     */
    ReportResponseDto updateReport(Long reportId, UpdateReportRequest request, AuthUser authUser);

    /**
     * 제보를 삭제합니다
     * USER, PROTECTOR: 자신이 생성한 제보만 삭제
     * ADMIN: 모든 제보 삭제
     */
    void deleteReport(Long reportId, AuthUser authUser);

    /**
     * 제보들을 일괄 삭제합니다
     * USER, PROTECTOR: 자신이 생성한 제보만 삭제
     * ADMIN: 모든 제보 삭제
     */
    void deleteReports(DeleteReportsRequest request, AuthUser authUser);

    /**
     * 관리자가 제보 상태를 변경합니다 (승인/반려 시 답변 포함)
     */
    ReportResponseDto updateReportStatus(Long reportId, UpdateReportStatusRequest request);
}


