package com.neighbus.config;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactoryCustomizer<TomcatServletWebServerFactory> cookieProcessorCustomizer() {
        return factory -> factory.addAdditionalTomcatConnectors(createStandardConnector());
    }

    private Connector createStandardConnector() {
        // HTTP 프로토콜을 사용하는 커넥터 생성
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080); // 추가할 HTTP 포트 번호
        connector.setSecure(false);
        return connector;
    }
}