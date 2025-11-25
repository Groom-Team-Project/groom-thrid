package groom.backend.common.exception;

import groom.backend.common.response.ApiResponse;
import groom.backend.common.response.StatusCodeMessage;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 예외 응답에 대한 공통 처리
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 입력값 검증 실패 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException e) {

        // 검증 실패한 필드 정보 추출
        List<ErrorDetail> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorDetail(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage(),
                        "C_001"  // 에러 코드 : 클라이언트 입력에러
                ))
                .toList();

        // 검증 실패 필드가 없는 경우
        if (errors.isEmpty()) {
            errors = List.of(new groom.backend.common.exception.ErrorDetail(
                    null,
                    null,
                    e.getMessage(),
                    "C_001" // 에러 코드 : 클라이언트 입력에러
            ));
        }

        ApiResponse<Void> response = ApiResponse.error(
            StatusCodeMessage.BAD_REQUEST.getCode(),
            StatusCodeMessage.BAD_REQUEST.getMessage(),
            errors
        );

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(response);
    }

    /**
     * 모든 예외의 기본 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        ApiResponse<Void> response = ApiResponse.error(
            StatusCodeMessage.INTERNAL_SERVER_ERROR.getCode(),
            StatusCodeMessage.INTERNAL_SERVER_ERROR.getMessage(),
            List.of(new ErrorDetail(
                null,
                null,
                e.getMessage(),
                "S_001"
            ))
        );

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(response);
    }
}
