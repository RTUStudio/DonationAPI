package kr.rtustudio.donation.service.chzzk.official.net.http.server.undertow;

import io.undertow.Undertow;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.session.InMemorySessionManager;
import io.undertow.server.session.SessionAttachmentHandler;
import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import kr.rtustudio.donation.service.chzzk.official.ChzzkAuthServer;
import kr.rtustudio.donation.service.chzzk.official.net.http.server.AuthServer;
import kr.rtustudio.donation.service.chzzk.official.net.http.server.AuthServerHandler;
import kr.rtustudio.donation.service.chzzk.official.net.http.server.undertow.exchange.AuthCallbackHandler;
import kr.rtustudio.donation.service.chzzk.official.net.http.server.undertow.exchange.AuthLoginChzzkHandler;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.xnio.Options;
import org.xnio.Sequence;

import java.util.UUID;

@Getter
public class UndertowAuthServer implements AuthServer {

    private final UUID uniqueId = UUID.randomUUID();

    private final SessionCookieConfig sessionCookieConfig = new SessionCookieConfig();
    private final SessionManager sessionManager = new InMemorySessionManager("CHZZK_SESSION_MANAGER_" + uniqueId);
    private final SessionAttachmentHandler sessionHandler = new SessionAttachmentHandler(sessionManager, sessionCookieConfig);
    private final RoutingHandler routing = new RoutingHandler();
    private final Undertow server;

    private final @NotNull ChzzkAuthServer chzzkAuthServer;
    private final @NotNull AuthServerHandler handler;

    public UndertowAuthServer(@NotNull ChzzkAuthServer chzzkAuthServer, @NotNull AuthServerHandler handler) {
        this.chzzkAuthServer = chzzkAuthServer;
        this.handler = handler;

        routing.get("/auth/login/chzzk", new AuthLoginChzzkHandler(this));
        routing.get("/auth/callback", new AuthCallbackHandler(this));

        PathHandler pathHandler = new PathHandler(sessionHandler);
        pathHandler.addPrefixPath("/", routing);

        server = Undertow.builder()
                .addHttpListener(chzzkAuthServer.getPort(), chzzkAuthServer.getHost())
                .setSocketOption(Options.SSL_ENABLED_PROTOCOLS, Sequence.of("TLSv1.2", "TLSv1.3"))
                .setHandler(pathHandler)
                .build();
    }

    @Override
    public void start() {
        server.start();
    }

    @Override
    public void stop() {
        server.stop();
    }

}
