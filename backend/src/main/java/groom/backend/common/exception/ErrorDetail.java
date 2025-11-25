package groom.backend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorDetail {
    private String field;
    private Object rejectedValue;
    private String reason;
    private String code;
}
