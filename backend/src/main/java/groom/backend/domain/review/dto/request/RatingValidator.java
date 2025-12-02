package groom.backend.domain.review.dto.request;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@jakarta.validation.Constraint(validatedBy = RatingValidator.RatingConstraintValidator.class)
public @interface RatingValidator {
    String message() default "평점은 0부터 5까지 0.5 단위로 입력 가능합니다 (예: 0, 0.5, 1, 1.5, ..., 5)";
    Class<?>[] groups() default {};
    Class<? extends jakarta.validation.Payload>[] payload() default {};

    class RatingConstraintValidator implements ConstraintValidator<RatingValidator, Double> {
        @Override
        public void initialize(RatingValidator constraintAnnotation) {
        }

        @Override
        public boolean isValid(Double value, ConstraintValidatorContext context) {
            if (value == null) {
                return true; // @NotNull과 함께 사용
            }
            
            // 0 ~ 5 범위 체크
            if (value < 0.0 || value > 5.0) {
                return false;
            }
            
            // 0.5 단위인지 체크 (0.5의 배수인지 확인)
            // 부동소수점 연산의 정밀도 문제를 고려하여 작은 오차 허용
            double remainder = value % 0.5;
            return Math.abs(remainder) < 0.001;
        }
    }
}
