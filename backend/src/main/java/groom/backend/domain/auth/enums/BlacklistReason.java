package groom.backend.domain.auth.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BlacklistReason {
    LOGOUT("사용자 로그아웃"),
    PASSWORD_CHANGE("비밀번호 변경"),
    ADMIN_FORCED("관리자 강제 로그아웃"),
    TOKEN_THEFT("토큰 도용 의심");

    private final String description;
}
