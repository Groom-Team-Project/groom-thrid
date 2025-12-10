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
import groom.backend.domain.users.entity.User;
import groom.backend.domain.users.repository.spec.UserRepository;
import groom.backend.common.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ReportRepository reportRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Override
    @Transactional
    public ReportResponseDto createReport(Long placeId, CreateReportRequest request, AuthUser authUser) {
        // 현재 인증된 사용자 조회
        User user = userRepository.findUserById(authUser.userId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. ID: " + authUser.userId()));
        
        // Base64 이미지를 S3에 업로드하고 URL로 변환
        String imageUrl = s3Service.uploadImageIfBase64(request.imageUrl(), "reports");

        // imageUrl이 업데이트된 CreateReportRequest 생성
        CreateReportRequest updatedRequest = new CreateReportRequest(
                request.content(),
                imageUrl
        );
        
        Report report = ReportMapper.toEntity(placeId, updatedRequest, user);
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
            // USER, PROTECTOR는 자신의 userId로 제보 조회
            List<Report> reports = reportRepository.findByUserId(authUser.userId());
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
            if (report.getUser() == null || !report.getUser().getId().equals(authUser.userId())) {
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
            if (report.getUser() == null || !report.getUser().getId().equals(authUser.userId())) {
                throw new IllegalArgumentException("본인의 제보만 수정할 수 있습니다.");
            }
        }
        
        // 기존 이미지가 S3에 있다면 삭제
        if (report.getImageUrl() != null && !report.getImageUrl().isBlank()) {
            s3Service.deleteImage(report.getImageUrl());
        }
        
        // Base64 이미지를 S3에 업로드하고 URL로 변환
        String imageUrl = s3Service.uploadImageIfBase64(request.imageUrl(), "reports");
        
        report.update(request.content(), imageUrl);
        
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
            if (report.getUser() == null || !report.getUser().getId().equals(authUser.userId())) {
                throw new IllegalArgumentException("본인의 제보만 삭제할 수 있습니다.");
            }
        }
        
        // S3에서 이미지 삭제
        if (report.getImageUrl() != null && !report.getImageUrl().isBlank()) {
            s3Service.deleteImage(report.getImageUrl());
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
            for (Long reportId : reportIds) {
                if (!reportRepository.existsByIdAndUserId(reportId, authUser.userId())) {
                    throw new IllegalArgumentException("제보 ID " + reportId + "는 본인의 제보가 아니거나 존재하지 않습니다.");
                }
            }
        }
        
        // 삭제 전에 모든 제보의 이미지를 S3에서 삭제
        for (Long reportId : reportIds) {
            reportRepository.findById(reportId).ifPresent(report -> {
                if (report.getImageUrl() != null && !report.getImageUrl().isBlank()) {
                    s3Service.deleteImage(report.getImageUrl());
                }
            });
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


