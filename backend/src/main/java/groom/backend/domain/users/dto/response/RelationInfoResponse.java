package groom.backend.domain.users.dto.response;

public record RelationInfoResponse(
        String UserName,
        String UserEmail,
        String GuardianName,
        String GuardianEmail
) {
}
