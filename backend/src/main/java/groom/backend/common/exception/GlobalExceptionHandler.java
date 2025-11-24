package groom.backend.common.exception;

import groom.backend.common.response.ApiResponse;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 전역 예외 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        ApiResponse<Void> response;
        response = ApiResponse.error(
                500,
                "서버 내부 오류가 발생했습니다",
                List.of(new ErrorDetail(null, null, e.getMessage(), "S_001"))
        );
        return ResponseEntity.status(500).body(response);
    }
}
