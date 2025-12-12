package groom.backend.common.exception;

/**
 * 리뷰를 찾을 수 없을 때 발생하는 예외
 */
public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long id) {
        super("리뷰를 찾을 수 없습니다. ID: " + id);
    }

    public ReviewNotFoundException(String message) {
        super(message);
    }

    public ReviewNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
