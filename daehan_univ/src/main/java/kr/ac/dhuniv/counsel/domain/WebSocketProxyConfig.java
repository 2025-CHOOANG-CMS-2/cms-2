package kr.ac.dhuniv.counsel.domain; // 패키지 경로는 프로젝트에 맞게 수정

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.socket.EnableWebSocketSecurity;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocketSecurity // Spring에서 웹소켓을 활성화하는 어노테이션
public class WebSocketProxyConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // "/ws-chat/**" 경로로 들어오는 모든 웹소켓 요청을 WebSocketProxyHandler가 처리하도록 등록합니다.
        registry.addHandler(new WebSocketProxyHandler(), "/ws-chat/**")
                .setAllowedOrigins("*"); // 모든 도메인에서의 접속을 허용합니다 (개발용)
    }
}