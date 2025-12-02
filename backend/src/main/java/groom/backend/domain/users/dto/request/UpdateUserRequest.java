package groom.backend.domain.users.dto.request;

import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class UpdateUserRequest {

    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다")
    private String name;

    private String phone;
}
