package groom.backend.common.response;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 정상 응답에 대한 공통 처리
 *
 */
@RestControllerAdvice
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // ApiResponse는 이미 ApiResponse이므로 처리하지 않음
        if (returnType.getParameterType().equals(ApiResponse.class)) {
            return false;
        }

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        // 이미 ApiResponse면 그냥 반환 : 이중검증
        if (body instanceof ApiResponse) {
            return body;
        }

        // Servlet 환경에서 상태코드 추출 -> webflux쪽 호환성 때문에 servlet 기술쪽은 캐스팅이 필요
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int statusCode = servletResponse.getStatus();

        // 상태코드에 해당하는 HttpStatusCode Enum 찾기
        StatusCodeMessage httpStatus = StatusCodeMessage.fromCode(statusCode);

        // ApiResponse로 감싸기 (Enum에서 정의한 메시지 자동 적용)
        return ApiResponse.success(statusCode, httpStatus.getMessage(), body);
    }
}
