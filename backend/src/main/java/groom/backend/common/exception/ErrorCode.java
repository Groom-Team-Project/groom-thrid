package groom.backend.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 비즈니스 예외 에러 코드 정의
 */
@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Auth 관련 에러 (A_xxx)
    DUPLICATE_EMAIL(409, "A_001", "이미 사용 중인 이메일입니다"),
    INVALID_CREDENTIALS(401, "A_002", "이메일 또는 비밀번호가 일치하지 않습니다"),
    DEACTIVATED_USER(403, "A_003", "비활성화된 계정입니다"),
    UNAUTHORIZED(401, "A_004", "인증이 필요합니다"),
    TOKEN_EXPIRED(401, "A_005", "토큰이 만료되었습니다"),
    INVALID_TOKEN(401, "A_006", "유효하지 않은 토큰입니다"),

    // Client/Validation 에러 (C_xxx)
    INVALID_INPUT(400, "C_001", "입력값이 올바르지 않습니다"),
    MISSING_PARAMETER(400, "C_002", "필수 파라미터가 누락되었습니다"),

    // Business Logic 에러 (B_xxx)
    RESOURCE_NOT_FOUND(404, "B_001", "요청한 리소스를 찾을 수 없습니다"),
    RESOURCE_CONFLICT(409, "B_002", "리소스 충돌이 발생했습니다"),

    // Server 에러 (S_xxx)
    INTERNAL_SERVER_ERROR(500, "S_001", "서버 내부 오류가 발생했습니다"),
    DATABASE_ERROR(500, "S_002", "데이터베이스 오류가 발생했습니다");

    private final int status;
    private final String code;
    private final String message;
}
