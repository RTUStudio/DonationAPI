package kr.rtustudio.donation.service.chzzk.impl;

import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.ChzzkAuthServer;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandlerHolder;
import kr.rtustudio.donation.service.chzzk.net.http.server.AuthServer;
import kr.rtustudio.donation.service.chzzk.net.http.server.AuthServerHandler;
import kr.rtustudio.donation.service.chzzk.net.http.server.undertow.AuthResult;
import kr.rtustudio.donation.service.chzzk.net.http.server.undertow.UndertowAuthServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

@Slf4j
@Getter
public class ChzzkAuthServerImpl implements ChzzkAuthServer, ChzzkEventHandlerHolder {

    private final @NotNull String clientId;
    private final @NotNull String clientSecret;
    private final @NotNull String baseUri;
    private final @NotNull String host;
    private final @Range(from = 0, to = 65535) int port;

    private final @NotNull ChzzkEventHandler handler;
    private final AuthServer server;

    ChzzkAuthServerImpl(
            @NotNull String clientId, @NotNull String clientSecret, @NotNull String baseUri,
            @NotNull String host, @Range(from = 0, to = 65535) int port,
            @NotNull ChzzkEventHandler handler
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseUri = baseUri;
        this.host = host;
        this.port = port;

        this.handler = handler;
        this.server = new UndertowAuthServer(this, new AuthServerHandler() {

            @Override
            public void onSuccess(@NotNull AuthServer server, @NotNull AuthResult result) {
                // 치지직 인스턴스 생성 (토큰 바인딩 안된 상태)
                Chzzk chzzk = Chzzk.builder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .eventHandler(handler)
                        .build();

                // 토큰 요청
                chzzk.grantToken(result).whenComplete((unused, throwable) -> {
                    if (throwable != null) {
                        log.error("ChzzkOfficial grant token failed", throwable);
                        handler.onUserRegisterFailed(result.type().toString(), throwable);
                        return;
                    }
                    log.info("ChzzkOfficial user registered: {}", result.user());
                    handler.onUserRegistered(chzzk, result.user());
                });
            }

            @Override
            public void onFailure(@NotNull AuthServer server, @NotNull AuthResult result) {
                handler.onUserRegisterFailed(result.type().toString(), null);
            }

        });
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
