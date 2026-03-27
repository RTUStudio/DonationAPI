package kr.rtustudio.donation.service.soop;

import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import kr.rtustudio.donation.service.soop.net.SoopAuthServerHandler;
import kr.rtustudio.donation.service.soop.net.handler.AuthCallbackSoopHandler;
import kr.rtustudio.donation.service.soop.net.handler.AuthLoginSoopHandler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.xnio.Options;
import org.xnio.Sequence;

import java.util.UUID;

@Getter
public class SoopAuthServer {

    private final UUID uniqueId = UUID.randomUUID();

    private final SessionCookieConfig sessionCookieConfig = new SessionCookieConfig();
    private final SessionManager sessionManager = new InMemorySessionManager("SOOP_SESSION_MANAGER_" + uniqueId);
    private final SessionAttachmentHandler sessionHandler = new SessionAttachmentHandler(sessionManager, sessionCookieConfig);
    private final RoutingHandler routing = new RoutingHandler();
    private final Undertow server;

    private final @NotNull String clientId;
    private final @NotNull String clientSecret;
    private final @NotNull String baseUri;
    private final @NotNull String host;
    private final @Range(from = 0, to = 65535) int port;
    private final @NotNull SoopAuthServerHandler handler;

    public SoopAuthServer(
            @NotNull String clientId, @NotNull String clientSecret, @NotNull String baseUri,
            @NotNull String host, @Range(from = 0, to = 65535) int port,
            @NotNull SoopAuthServerHandler handler
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseUri = baseUri;
        this.host = host;
        this.port = port;
        this.handler = handler;

        routing.get("/auth/login/soop", new AuthLoginSoopHandler(this));
        routing.get("/auth/callback/soop", new AuthCallbackSoopHandler(this));

        PathHandler pathHandler = new PathHandler(sessionHandler);
        pathHandler.addPrefixPath("/", routing);

        server = Undertow.builder()
                .addHttpListener(port, host)
                .setSocketOption(Options.SSL_ENABLED_PROTOCOLS, Sequence.of("TLSv1.2", "TLSv1.3"))
                .setHandler(pathHandler)
                .build();
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

}
