package groom.backend.common.config;

import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 웹 설정 - 요청 본문 크기 제한 설정
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Tomcat 서버의 요청 본문 크기 제한을 늘리는 설정
     * application.yml의 server.tomcat.max-http-post-size와 함께 작동
     */
    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> tomcatCustomizer() {
        return factory -> {
            factory.addConnectorCustomizers(connector -> {
                connector.setMaxPostSize(50 * 1024 * 1024); // 50MB
            });
        };
    }
}
