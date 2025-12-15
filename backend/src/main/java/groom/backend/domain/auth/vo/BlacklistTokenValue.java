package groom.backend.domain.auth.vo;

import groom.backend.domain.auth.enums.BlacklistReason;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class BlacklistTokenValue implements Serializable {

    private UUID userId;
    private BlacklistReason reason;
    private LocalDateTime blacklistedAt;

    public static BlacklistTokenValue of(UUID userId, BlacklistReason reason) {
        return new BlacklistTokenValue(userId, reason, LocalDateTime.now());
    }
}
