package kr.rtustudio.donation.service.soop.net.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import kr.rtustudio.donation.service.auth.AuthResponsePage;
import kr.rtustudio.donation.service.soop.SoopAuthServer;
import kr.rtustudio.donation.service.soop.net.AuthResult;
import kr.rtustudio.donation.service.soop.utils.HttpExchangeQueryParameterParser;
import org.jetbrains.annotations.NotNull;

public class AuthCallbackSoopHandler implements HttpHandler {

    private static final String SERVICE_NAME = "숲 (SOOP)";

    private final @NotNull SoopAuthServer authServer;

    public AuthCallbackSoopHandler(@NotNull SoopAuthServer authServer) {
        this.authServer = authServer;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        SessionManager manager = authServer.getSessionManager();
        SessionCookieConfig cookieConfig = authServer.getSessionCookieConfig();

        Session session = manager.getSession(exchange, cookieConfig);
        if (session == null) {
            session = manager.createSession(exchange, cookieConfig);
        }

        String code = HttpExchangeQueryParameterParser.parse(exchange, "code");

        if (code == null) {
            sendFailure(exchange, "인증 코드를 받지 못했습니다.");
            AuthResult result = new AuthResult("", AuthResult.Type.INVALID_CODE_PARAMETER, null);
            authServer.getHandler().onFailure(result);
            return;
        }

        // 로그인 성공 → 서비스 연결 시도
        String user = (String) session.getAttribute("USER");
        AuthResult result = new AuthResult(code, AuthResult.Type.SUCCESS, user);
        boolean connected = authServer.getHandler().onSuccess(result);

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=UTF-8");
        if (connected) {
            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseSender().send(AuthResponsePage.success(SERVICE_NAME));
        } else {
            exchange.setStatusCode(StatusCodes.OK);
            exchange.getResponseSender().send(AuthResponsePage.failure(SERVICE_NAME, "연동에 실패했습니다. 잠시 후 다시 시도해주세요."));
        }
    }

    private void sendFailure(HttpServerExchange exchange, String reason) {
        exchange.setStatusCode(StatusCodes.BAD_REQUEST);
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html; charset=UTF-8");
        exchange.getResponseSender().send(AuthResponsePage.failure(SERVICE_NAME, reason));
    }

}
