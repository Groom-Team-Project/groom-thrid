package groom.backend.domain.auth.vo;

import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenValue implements Serializable {

    private UUID userId;

    public static RefreshTokenValue of(UUID userId) {
        return new RefreshTokenValue(userId);
    }
}
