package groom.backend.domain.report.service.impl;

import groom.backend.common.security.AuthUser;
import groom.backend.domain.report.dto.request.CreateReportRequest;
import groom.backend.domain.report.dto.request.DeleteReportsRequest;
import groom.backend.domain.report.dto.request.UpdateReportRequest;
import groom.backend.domain.report.dto.request.UpdateReportStatusRequest;
import groom.backend.domain.report.dto.response.ReportResponseDto;
import groom.backend.domain.report.entity.Report;
import groom.backend.domain.report.entity.ReportStatus;
import groom.backend.domain.report.mapper.ReportMapper;
import groom.backend.domain.report.repository.spec.ReportRepository;
import groom.backend.domain.report.service.spec.ReportService;
import groom.backend.domain.users.entity.Role;
import groom.backend.domain.users.repository.spec.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ReportResponseDto createReport(Long placeId, CreateReportRequest request, AuthUser authUser) {
        Report report = ReportMapper.toEntity(placeId, request);
        Report savedReport = reportRepository.save(report);
        return ReportMapper.toResponseDto(savedReport);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReportResponseDto> getReports(AuthUser authUser) {
        // ADMIN: 모든 제보 목록 조회
        // USER, PROTECTOR: 자신이 생성한 제보 목록만 조회
        if (authUser.role() == Role.ADMIN) {
            List<Report> reports = reportRepository.findAll();
            return ReportMapper.toResponseDtoList(reports);
        } else {
            // USER, PROTECTOR는 자신의 이름으로 제보 조회
            String userName = userRepository.findById(authUser.userId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                    .getName();
            List<Report> reports = reportRepository.findByAuthor(userName);
            return ReportMapper.toResponseDtoList(reports);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public ReportResponseDto getReport(Long reportId, AuthUser authUser) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("제보를 찾을 수 없습니다. ID: " + reportId));
        
        // ADMIN: 모든 제보 조회 가능
        // USER, PROTECTOR: 자신이 생성한 제보만 조회 가능
        if (authUser.role() != Role.ADMIN) {
            String userName = userRepository.findById(authUser.userId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                    .getName();
            if (!report.getAuthor().equals(userName)) {
                throw new IllegalArgumentException("본인의 제보만 조회할 수 있습니다.");
            }
        }
        
        return ReportMapper.toResponseDto(report);
    }

    @Override
    @Transactional
    public ReportResponseDto updateReport(Long reportId, UpdateReportRequest request, AuthUser authUser) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("제보를 찾을 수 없습니다. ID: " + reportId));
        
        // ADMIN: 모든 제보 수정 가능
        // USER, PROTECTOR: 자신이 생성한 제보만 수정 가능
        if (authUser.role() != Role.ADMIN) {
            String userName = userRepository.findById(authUser.userId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                    .getName();
            if (!report.getAuthor().equals(userName)) {
                throw new IllegalArgumentException("본인의 제보만 수정할 수 있습니다.");
            }
        }
        
        report.update(request.content(), request.imageUrl());
        
        Report savedReport = reportRepository.save(report);
        return ReportMapper.toResponseDto(savedReport);
    }

    @Override
    @Transactional
    public void deleteReport(Long reportId, AuthUser authUser) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new IllegalArgumentException("제보를 찾을 수 없습니다. ID: " + reportId));
        
        // ADMIN: 모든 제보 삭제 가능
        // USER, PROTECTOR: 자신이 생성한 제보만 삭제 가능
        if (authUser.role() != Role.ADMIN) {
            String userName = userRepository.findById(authUser.userId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                    .getName();
            if (!report.getAuthor().equals(userName)) {
                throw new IllegalArgumentException("본인의 제보만 삭제할 수 있습니다.");
            }
        }
        
        reportRepository.deleteById(reportId);
    }

    @Override
    @Transactional
    public void deleteReports(DeleteReportsRequest request, AuthUser authUser) {
        List<Long> reportIds = request.reportIds();
        
        // ADMIN: 모든 제보 삭제 가능
        // USER, PROTECTOR: 자신이 생성한 제보만 삭제 가능
        if (authUser.role() == Role.ADMIN) {
            // 모든 제보가 존재하는지 확인
            for (Long reportId : reportIds) {
                if (!reportRepository.existsById(reportId)) {
                    throw new IllegalArgumentException("제보 ID " + reportId + "를 찾을 수 없습니다.");
                }
            }
        } else {
            // USER, PROTECTOR는 자신의 제보만 삭제 가능
            String userName = userRepository.findById(authUser.userId())
                    .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."))
                    .getName();
            for (Long reportId : reportIds) {
                if (!reportRepository.existsByIdAndAuthor(reportId, userName)) {
                    throw new IllegalArgumentException("제보 ID " + reportId + "는 본인의 제보가 아니거나 존재하지 않습니다.");
                }
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
}


