package kr.rtustudio.donation.service.chzzk.net.http.server.undertow.exchange;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import kr.rtustudio.donation.service.chzzk.ChzzkAuthServer;
import kr.rtustudio.donation.service.chzzk.net.http.server.AuthServer;
import kr.rtustudio.donation.service.chzzk.utils.CSRFTokenGenerator;
import kr.rtustudio.donation.service.chzzk.utils.HttpExchangeQueryParameterParser;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

public class AuthLoginChzzkHandler implements HttpHandler {

    private final @NotNull AuthServer server;

    public AuthLoginChzzkHandler(@NotNull AuthServer chzzk) {
        this.server = chzzk;
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        SessionManager manager = server.getSessionManager();
        SessionCookieConfig cookieConfig = server.getSessionCookieConfig();

        Session session = manager.getSession(exchange, cookieConfig);
        if (session == null) {
            session = manager.createSession(exchange, cookieConfig);
        }

        String user = HttpExchangeQueryParameterParser.parse(exchange, "user");
        if (user != null) {
            session.setAttribute("USER", user);
        }

        // CSRF 방지를 위한 state 값 생성
        String state = CSRFTokenGenerator.generateDefault();
        session.setAttribute("OAUTH_STATE", state);

        ChzzkAuthServer chzzk = server.getChzzkAuthServer();
        HttpUrl url = HttpUrl.get("https://chzzk.naver.com/account-interlock")
                .newBuilder()
                .addQueryParameter("clientId", chzzk.getClientId())
                .addQueryParameter("redirectUri", chzzk.getBaseUri() + "/auth/callback")
                .addQueryParameter("state", state)
                .build();

        exchange.setStatusCode(StatusCodes.FOUND); // 302 redirect
        exchange.getResponseHeaders().put(Headers.LOCATION, url.toString());
        exchange.endExchange();
    }

}
