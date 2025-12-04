package groom.backend.domain.report.service.impl;

import groom.backend.domain.report.dto.request.CreateReportRequest;
import groom.backend.domain.report.dto.request.UpdateReportRequest;
import groom.backend.domain.report.dto.request.UpdateReportStatusRequest;
import groom.backend.domain.report.dto.response.ReportResponseDto;
import groom.backend.domain.report.entity.Report;
import groom.backend.domain.report.entity.ReportStatus;
import groom.backend.domain.report.mapper.ReportMapper;
import groom.backend.domain.report.repository.spec.ReportRepository;
import groom.backend.domain.report.service.spec.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;

    @Override
    @Transactional
    public ReportResponseDto createReport(Long placeId, CreateReportRequest request) {
        Report report = ReportMapper.toEntity(placeId, request);
        Report savedReport = reportRepository.save(report);
        return ReportMapper.toResponseDto(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getMyReports(String author) {
        List<Report> reports = reportRepository.findByAuthor(author);
        return ReportMapper.toResponseDtoList(reports);
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDto getMyReport(Long reportId, String author) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("제보를 찾을 수 없습니다. ID: " + reportId));
        
        // 작성자 확인
        if (!report.getAuthor().equals(author)) {
            throw new IllegalArgumentException("본인의 제보만 조회할 수 있습니다.");
        }
        
        return ReportMapper.toResponseDto(report);
    }

    @Override
    @Transactional
    public ReportResponseDto updateMyReport(Long reportId, String author, UpdateReportRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("제보를 찾을 수 없습니다. ID: " + reportId));
        
        // 작성자 확인
        if (!report.getAuthor().equals(author)) {
            throw new IllegalArgumentException("본인의 제보만 수정할 수 있습니다.");
        }
        
        // 제보 수정
        report.update(request.content(), request.imageUrl());
        
        Report savedReport = reportRepository.save(report);
        return ReportMapper.toResponseDto(savedReport);
    }

    @Override
    @Transactional
    public void deleteMyReport(Long reportId, String author) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("제보를 찾을 수 없습니다. ID: " + reportId));
        
        // 작성자 확인
        if (!report.getAuthor().equals(author)) {
            throw new IllegalArgumentException("본인의 제보만 삭제할 수 있습니다.");
        }
        
        reportRepository.deleteById(reportId);
    }

    @Override
    @Transactional
    public void deleteMyReports(List<Long> reportIds, String author) {
        // 모든 제보가 해당 작성자의 것인지 확인
        for (Long reportId : reportIds) {
            if (!reportRepository.existsByIdAndAuthor(reportId, author)) {
                throw new IllegalArgumentException("제보 ID " + reportId + "는 본인의 제보가 아니거나 존재하지 않습니다.");
            }
        }
        
        reportRepository.deleteAllById(reportIds);
    }

    @Override
    @Transactional
    public ReportResponseDto updateReportStatus(Long reportId, UpdateReportStatusRequest request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("제보를 찾을 수 없습니다. ID: " + reportId));

        // 승인 또는 반려 시 답변 필수
        if ((request.status() == ReportStatus.APPROVED || request.status() == ReportStatus.REJECTED)) {
            if (request.adminReply() == null || request.adminReply().isBlank()) {
                throw new IllegalArgumentException("승인 또는 반려 시 관리자 답변은 필수입니다.");
            }
        }

        // 상태 변경 및 답변 설정
        report.updateStatusWithReply(request.status(), request.adminReply());

        Report savedReport = reportRepository.save(report);
        return ReportMapper.toResponseDto(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getAllReports() {
        List<Report> reports = reportRepository.findAll();
        return ReportMapper.toResponseDtoList(reports);
    }
}

