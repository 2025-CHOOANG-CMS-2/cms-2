package kr.ac.dhuniv.counsel.domain; // 위와 동일한 패키지 경로

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.WebSocketHttpHeaders;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Objects;

public class WebSocketProxyHandler extends TextWebSocketHandler {

    // 접속할 Node.js 서버의 주소. 'chat-server-container'는 Docker 컨테이너 이름입니다.
    // A 단계에서 설정한 Docker 네트워크 덕분에 컨테이너 이름으로 접속이 가능합니다.
    private static final String NODE_JS_SERVER_URI = "ws://chat-server-container:3001/socket.io/?EIO=4&transport=websocket";

    // [클라이언트-스프링] 세션과 [스프링-Node.js] 세션을 짝지어 저장할 맵
    private final Map<String, WebSocketSession> proxySessions = new ConcurrentHashMap<>();
    private final Map<String, WebSocketSession> clientSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession clientSession) throws Exception {
        URI uri = new URI(NODE_JS_SERVER_URI);

        // Node.js 서버로 연결할 프록시 세션을 생성합니다.
        WebSocketSession proxySession = new StandardWebSocketClient().doHandshake(
            new TextWebSocketHandler() {
                // Node.js 서버로부터 메시지를 받으면, 원래 클라이언트에게 전달합니다.
                @Override
                protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
                    WebSocketSession originalClient = clientSessions.get(session.getId());
                    if (originalClient != null && originalClient.isOpen()) {
                        originalClient.sendMessage(message);
                    }
                }
            }, new WebSocketHttpHeaders(), uri).get();

        // 두 세션을 서로의 ID를 키로 하여 맵에 저장합니다.
        proxySessions.put(clientSession.getId(), proxySession);
        clientSessions.put(proxySession.getId(), clientSession);
    }

    @Override
    protected void handleTextMessage(WebSocketSession clientSession, TextMessage message) throws Exception {
        // 클라이언트로부터 메시지를 받으면, 연결된 프록시 세션을 통해 Node.js 서버로 전달합니다.
        WebSocketSession proxySession = proxySessions.get(clientSession.getId());
        if (proxySession != null && proxySession.isOpen()) {
            proxySession.sendMessage(message);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession clientSession, CloseStatus status) throws Exception {
        // 클라이언트와 연결이 끊기면, Node.js 서버와의 연결도 함께 끊고 맵에서 제거합니다.
        WebSocketSession proxySession = proxySessions.remove(clientSession.getId());
        if (proxySession != null) {
            clientSessions.remove(proxySession.getId());
            if (proxySession.isOpen()) {
                proxySession.close(status);
            }
        }
    }
}