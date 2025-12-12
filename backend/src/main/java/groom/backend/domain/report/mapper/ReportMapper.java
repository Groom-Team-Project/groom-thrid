package groom.backend.domain.report.mapper;

import groom.backend.domain.report.dto.request.CreateReportRequest;
import groom.backend.domain.report.dto.response.ReportResponseDto;
import groom.backend.domain.report.entity.Report;
import groom.backend.domain.report.entity.ReportStatus;
import groom.backend.domain.users.entity.User;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReportMapper {

    public static Report toEntity(Long placeId, CreateReportRequest request, User user) {
        return Report.builder()
                .placeId(placeId)
                .content(request.content())
                .user(user)
                .author(user.getName()) // User의 이름으로 author 설정
                .imageUrl(request.imageUrl())
                .status(ReportStatus.PENDING) // 기본값: 대기 중
                .build();
    }

    public static ReportResponseDto toResponseDto(Report report) {
        // User 엔티티에서 이름을 가져오되, 없으면 denormalized author 사용
        String authorName = report.getUser() != null ? report.getUser().getName() : report.getAuthor();
        // User 엔티티에서 이메일을 가져옴
        String authorEmail = report.getUser() != null ? report.getUser().getEmail() : null;
        
        return new ReportResponseDto(
                report.getId(),
                report.getPlaceId(),
                report.getContent(),
                authorName,
                authorEmail,
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


