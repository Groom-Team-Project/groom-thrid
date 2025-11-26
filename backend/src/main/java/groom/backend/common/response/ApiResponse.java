package groom.backend.common.response;


import groom.backend.common.exception.ErrorDetail;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse<T> {
    private String status;
    private int code;
    private String message;
    private T data;
    private List<ErrorDetail> errors;

    // 성공 응답
    public static <T> ApiResponse<T> success(int code, String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.status = "success";
        response.code = code;
        response.message = message;
        response.data = data;
        response.errors = null;
        return response;
    }

    // 실패 응답
    public static <Void> ApiResponse<Void> error(int code, String message, List<ErrorDetail> errors) {
        ApiResponse<Void> response = new ApiResponse<>();
        response.status = "error";
        response.code = code;
        response.message = message;
        response.data = null;
        response.errors = errors;
        return response;
    }
}
