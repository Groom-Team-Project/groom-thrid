package groom.backend.domain.users.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Provider {
    Form("Form 로그인"),
    Naver("네이버 OAuth"),
    Google("구글 OAuth"),
    Kakao("카카오 OAuth");

    private final String description;
}
