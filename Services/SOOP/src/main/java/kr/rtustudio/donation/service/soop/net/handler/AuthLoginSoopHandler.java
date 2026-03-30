package kr.rtustudio.donation.service.soop.net.handler;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.session.Session;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import kr.rtustudio.donation.service.soop.SoopAuthServer;
import kr.rtustudio.donation.service.soop.utils.Constants;
import kr.rtustudio.donation.service.soop.utils.HttpExchangeQueryParameterParser;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

public class AuthLoginSoopHandler implements HttpHandler {

    private final @NotNull SoopAuthServer authServer;

    public AuthLoginSoopHandler(@NotNull SoopAuthServer authServer) {
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

        String user = HttpExchangeQueryParameterParser.parse(exchange, "user");
        if (user != null) {
            session.setAttribute("USER", user);
        }

        HttpUrl url = HttpUrl.get(Constants.OPENAPI_URL + "/auth/code")
                .newBuilder()
                .addQueryParameter("client_id", authServer.getClientId())
                .addQueryParameter("response_type", "code")
                .build();

        exchange.setStatusCode(StatusCodes.FOUND); // 302 redirect
        exchange.getResponseHeaders().put(Headers.LOCATION, url.toString());
        exchange.endExchange();
    }

}
