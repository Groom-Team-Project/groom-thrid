package groom.backend.domain.report.service.spec;

import groom.backend.domain.report.dto.request.CreateReportRequest;
import groom.backend.domain.report.dto.request.UpdateReportRequest;
import groom.backend.domain.report.dto.request.UpdateReportStatusRequest;
import groom.backend.domain.report.dto.response.ReportResponse;

import java.util.List;

public interface ReportService {
    /**
     * 새로운 제보를 생성합니다
     */
    ReportResponse createReport(Long placeId, CreateReportRequest request);

    /**
     * 나의 제보 목록을 조회합니다
     */
    List<ReportResponse> getMyReports(String author);

    /**
     * 나의 제보 상세를 조회합니다
     */
    ReportResponse getMyReport(Long reportId, String author);

    /**
     * 나의 제보를 수정합니다
     */
    ReportResponse updateMyReport(Long reportId, String author, UpdateReportRequest request);

    /**
     * 나의 제보를 삭제합니다
     */
    void deleteMyReport(Long reportId, String author);

    /**
     * 나의 제보들을 삭제합니다
     */
    void deleteMyReports(List<Long> reportIds, String author);

    /**
     * 관리자가 제보 상태를 변경합니다 (승인/반려 시 답변 포함)
     */
    ReportResponse updateReportStatus(Long reportId, UpdateReportStatusRequest request);

    /**
     * 관리자가 모든 제보 목록을 조회합니다
     */
    List<ReportResponse> getAllReports();
}

