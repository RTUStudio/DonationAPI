package kr.rtustudio.donation.service.chzzk.net.http.server;

import io.undertow.server.session.SessionCookieConfig;
import io.undertow.server.session.SessionManager;
import kr.rtustudio.donation.service.chzzk.ChzzkAuthServer;
import org.jetbrains.annotations.NotNull;

public interface AuthServer {

    @NotNull ChzzkAuthServer getChzzkAuthServer();

    @NotNull SessionManager getSessionManager();

    @NotNull SessionCookieConfig getSessionCookieConfig();

    @NotNull AuthServerHandler getHandler();

    void start();

    void stop();

}
