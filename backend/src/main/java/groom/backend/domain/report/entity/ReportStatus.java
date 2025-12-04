package groom.backend.domain.report.entity;

public enum ReportStatus {
    PENDING("대기 중"),
    PROCESSING("처리 중"),
    APPROVED("승인"),
    REJECTED("반려");

    private final String description;

    ReportStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}

