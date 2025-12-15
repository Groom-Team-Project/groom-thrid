package groom.backend.common.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "oauth")
@Getter
@Setter
public class OAuthConfig {

    private Map<String, ProviderConfig> providers = new HashMap<>();

    // Naver, Google, Kakao 설정을 Map으로 관리
    private ProviderConfig naver = new ProviderConfig();
    private ProviderConfig google = new ProviderConfig();
    private ProviderConfig kakao = new ProviderConfig();

    @Getter
    @Setter
    public static class ProviderConfig {
        private String clientId;
        private String clientSecret;
        private String redirectUri;
        private String tokenUrl;
        private String userInfoUrl;
    }

    // Provider별 설정 조회
    public ProviderConfig getProviderConfig(String provider) {
        return switch (provider.toLowerCase()) {
            case "naver" -> naver;
            case "google" -> google;
            case "kakao" -> kakao;
            default -> throw new IllegalArgumentException("Unknown OAuth provider: " + provider);
        };
    }
}
