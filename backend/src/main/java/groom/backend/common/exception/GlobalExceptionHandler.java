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

        String customCode = "C_001";
        StatusCodeMessage codeMessage = StatusCodeMessage.INPUT_ERROR;
        int code = codeMessage.getCode();
        String message = codeMessage.getMessage();

        // 검증 실패한 필드 정보 추출
        List<ErrorDetail> errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorDetail(
                        error.getField(),
                        error.getRejectedValue(),
                        error.getDefaultMessage(),
                        customCode  // 에러 코드 : 클라이언트 입력에러
                ))
                .toList();

        // 검증 실패 필드가 없는 경우
        if (errors.isEmpty()) {
            errors = List.of(new groom.backend.common.exception.ErrorDetail(
                    null,
                    null,
                    e.getMessage(),
                    customCode // 에러 코드 : 클라이언트 입력에러
            ));
        }

        ApiResponse<Void> response = ApiResponse.error(
                code,
                message,
                errors
        );

        return ResponseEntity
                .status(code)
                .body(response);
    }

    // BusinessException 처리 (비즈니스 로직 예외)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException e) {
        ErrorCode errorCode = e.getErrorCode();

        ApiResponse<Void> response = ApiResponse.error(
                errorCode.getStatus(),
                errorCode.getMessage(),
                List.of(new ErrorDetail(
                        null,
                        null,
                        e.getMessage(),
                        errorCode.getCode()
                ))
        );

        return ResponseEntity
                .status(errorCode.getStatus())
                .body(response);
    }

    /**
     * 사용자 NotFoundException 처리 (404 Not Found)
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleUserNotFoundException(UserNotFoundException e) {
        ApiResponse<Void> response = ApiResponse.error(
                StatusCodeMessage.NOT_FOUND.getCode(),
                StatusCodeMessage.NOT_FOUND.getMessage(),
                List.of(new ErrorDetail(
                        null,
                        null,
                        e.getMessage(),
                        "B_001"  // Business logic error
                ))
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    /**
     * 제보 NotFoundException 처리 (404 Not Found)
     */
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleReportNotFoundException(ReportNotFoundException e) {
        ApiResponse<Void> response = ApiResponse.error(
                StatusCodeMessage.NOT_FOUND.getCode(),
                StatusCodeMessage.NOT_FOUND.getMessage(),
                List.of(new ErrorDetail(
                        null,
                        null,
                        e.getMessage(),
                        "R_001"  // Report not found error
                ))
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
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
