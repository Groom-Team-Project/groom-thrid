package groom.backend.domain.report.mapper;

import groom.backend.domain.report.dto.request.CreateReportRequest;
import groom.backend.domain.report.dto.response.ReportResponseDto;
import groom.backend.domain.report.entity.Report;
import groom.backend.domain.report.entity.ReportStatus;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportMapper {

    public static Report toEntity(Long placeId, CreateReportRequest request) {
        return Report.builder()
                .placeId(placeId)
                .content(request.content())
                .author(request.author())
                .imageUrl(request.imageUrl())
                .status(ReportStatus.PENDING) // 기본값: 대기 중
                .build();
    }

    public static ReportResponseDto toResponseDto(Report report) {
        return new ReportResponseDto(
                report.getId(),
                report.getPlaceId(),
                report.getContent(),
                report.getAuthor(),
                report.getStatus().getDescription(),
                report.getImageUrl(),
                report.getAdminReply(),
                report.getCreatedAt(),
                report.getUpdatedAt()
        );
    }

    public static List<ReportResponseDto> toResponseDtoList(List<Report> reports) {
        return reports.stream()
                .map(ReportMapper::toResponseDto)
                .toList();
    }
}

