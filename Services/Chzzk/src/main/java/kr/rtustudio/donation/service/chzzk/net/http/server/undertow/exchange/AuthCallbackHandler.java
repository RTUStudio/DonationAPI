package kr.rtustudio.donation.service.chzzk.net.http.server.undertow.exchange;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import kr.rtustudio.donation.service.auth.AuthResponsePage;
import kr.rtustudio.donation.service.chzzk.net.http.server.AuthServer;
import kr.rtustudio.donation.service.chzzk.net.http.server.undertow.AuthResult;
import kr.rtustudio.donation.service.chzzk.utils.HttpExchangeQueryParameterParser;
import org.jetbrains.annotations.NotNull;

public class AuthCallbackHandler implements HttpHandler {

    private static final String SERVICE_NAME = "치지직 (Chzzk)";

    private final @NotNull AuthServer server;

    public AuthCallbackHandler(@NotNull AuthServer server) {
        this.server = server;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        SessionManager manager = server.getSessionManager();
        SessionCookieConfig cookieConfig = server.getSessionCookieConfig();

        Session session = manager.getSession(exchange, cookieConfig);
        if (session == null) {
            session = manager.createSession(exchange, cookieConfig);
        }

        String code = HttpExchangeQueryParameterParser.parse(exchange, "code");
        String state = HttpExchangeQueryParameterParser.parse(exchange, "state");

        if (code == null) {
            sendFailure(exchange, "인증 코드를 받지 못했습니다.");
            AuthResult result = new AuthResult("", "", AuthResult.Type.INVALID_CODE_PARAMETER, null);
            server.getHandler().onFailure(server, result);
            return;
        }

        // 세션에 저장된 state와 비교
        String savedState = (String) session.getAttribute("OAUTH_STATE");
        if (savedState == null || !savedState.equals(state)) {
            sendFailure(exchange, "인증 상태 값이 일치하지 않습니다. 다시 시도해주세요.");
            AuthResult result = new AuthResult("", "", AuthResult.Type.INVALID_STATE_PARAMETER, null);
            server.getHandler().onFailure(server, result);
            return;
        }

        // 로그인 성공 → 서비스 연결 시도
        String user = (String) session.getAttribute("USER");
        AuthResult result = new AuthResult(code, state, AuthResult.Type.SUCCESS, user);
        boolean connected = server.getHandler().onSuccess(server, result);

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=UTF-8");
        if (connected) {
            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseSender().send(AuthResponsePage.success(SERVICE_NAME));
        } else {
            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseSender().send(AuthResponsePage.failure(SERVICE_NAME, "연동에 실패했습니다. 다시 시도해주세요."));
        }
    }

    private void sendFailure(HttpServerExchange exchange, String reason) {
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=UTF-8");
        exchange.getResponseSender().send(AuthResponsePage.failure(SERVICE_NAME, reason));
    }

}
