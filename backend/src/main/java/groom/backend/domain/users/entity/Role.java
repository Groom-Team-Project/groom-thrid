package groom.backend.domain.users.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    USER("일반사용자"),
    ADMIN("관리자"),
    PROTECTOR("보호자");

    private final String description;
}
