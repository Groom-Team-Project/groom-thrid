package groom.backend.common.exception;

/**
 * 제보를 찾을 수 없을 때 발생하는 예외
 */
public class ReportNotFoundException extends RuntimeException {

    public ReportNotFoundException(Long id) {
        super("제보를 찾을 수 없습니다. ID: " + id);
    }

    public ReportNotFoundException(String message) {
        super(message);
    }

    public ReportNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
