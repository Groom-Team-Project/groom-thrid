package groom.backend.common.response;

import lombok.Getter;

/**
 * HTTP 상태코드와 기본 메시지를 관리하는 Enum
 */
@Getter
public enum StatusCodeMessage {

    // 2xx Success
    OK(200, "요청이 정상 처리되었습니다"),
    CREATED(201, "리소스가 생성되었습니다"),
    ACCEPTED(202, "요청이 수락되었습니다"),
    NO_CONTENT(204, "리소스가 삭제되었습니다"),

    // 4xx Client Error
    BAD_REQUEST(400, "잘못된 요청입니다"),
    UNAUTHORIZED(401, "인증이 필요합니다"),
    FORBIDDEN(403, "접근 권한이 없습니다"),
    NOT_FOUND(404, "요청한 리소스를 찾을 수 없습니다"),
    CONFLICT(409, "요청 데이터와 기존 데이터가 충돌합니다"),
    UNPROCESSABLE_ENTITY(422, "요청 데이터를 처리할 수 없습니다"),

    // 5xx Server Error
    INTERNAL_SERVER_ERROR(500, "서버 내부 오류가 발생했습니다"),
    BAD_GATEWAY(502, "게이트웨이 오류가 발생했습니다"),
    SERVICE_UNAVAILABLE(503, "서비스가 일시적으로 이용 불가합니다"),

    // custom Error
    INPUT_ERROR(400, "요청 형식이 잘못되었습니다.(DTO)");

    private final int code;
    private final String message;

    /**
     * Enum 생성자
     *
     * @param code HTTP 상태코드
     * @param message 기본 메시지
     */
    StatusCodeMessage(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * HTTP 상태코드로부터 Enum 값 찾기
     *
     * @param code HTTP 상태코드 (예: 200, 201, 404)
     * @return 해당하는 HttpStatusCode Enum
     *         없으면 INTERNAL_SERVER_ERROR 반환
     */
    public static StatusCodeMessage fromCode(int code) {
        for (StatusCodeMessage status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return INTERNAL_SERVER_ERROR;  // 일치하는
    }
}
