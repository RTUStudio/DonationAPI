package kr.rtustudio.donation.service.chzzk.impl;

import com.google.common.collect.ImmutableSet;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.ChzzkAuthServer;
import kr.rtustudio.donation.service.chzzk.ChzzkTokenMutator;
import kr.rtustudio.donation.service.chzzk.data.ChzzkToken;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandlerHolder;
import kr.rtustudio.donation.service.chzzk.net.data.AccessTokenGrantRequest;
import kr.rtustudio.donation.service.chzzk.net.data.AccessTokenGrantResponse;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.net.http.server.AuthServer;
import kr.rtustudio.donation.service.chzzk.net.http.server.AuthServerHandler;
import kr.rtustudio.donation.service.chzzk.net.http.server.undertow.AuthResult;
import kr.rtustudio.donation.service.chzzk.net.http.server.undertow.UndertowAuthServer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Optional;
import java.util.Set;

@Slf4j
@Getter
public class ChzzkAuthServerImpl implements ChzzkAuthServer, ChzzkEventHandlerHolder {

    private final @NotNull String clientId;
    private final @NotNull String clientSecret;
    private final @NotNull String baseUri;
    private final @NotNull String host;
    private final @Range(from = 0, to = 65535) int port;
    private final @Nullable SocketOption socketOption;

    private final @NotNull ImmutableSet<ChzzkEventHandler> handlers;
    private final AuthServer server;

    ChzzkAuthServerImpl(
            @NotNull String clientId, @NotNull String clientSecret, @NotNull String baseUri,
            @NotNull String host, @Range(from = 0, to = 65535) int port,
            @Nullable SocketOption socketOption,
            @NotNull Set<ChzzkEventHandler> handlers
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.baseUri = baseUri;
        this.host = host;
        this.port = port;
        this.socketOption = socketOption;

        this.handlers = ImmutableSet.copyOf(handlers);
        this.server = new UndertowAuthServer(this, new AuthServerHandler() {

            @Override
            public boolean onSuccess(@NotNull AuthServer server, @NotNull AuthResult result) {
                // 치지직 인스턴스 생성 (토큰 바인딩 안된 상태)
                Chzzk chzzk = Chzzk.builder()
                        .clientId(clientId)
                        .clientSecret(clientSecret)
                        .socketOption(socketOption)
                        .addEventHandler(handlers)
                        .build();

                // 토큰 요청 (동기)
                Optional<HttpRequestExecutor<AccessTokenGrantRequest, AccessTokenGrantResponse, OkHttpClient>> requester =
                        chzzk.getHttpRequestExecutorFactory().create("access_token_grant");

                if (requester.isEmpty()) {
                    log.error("CHZZK access_token_grant executor not found");
                    return false;
                }

                AccessTokenGrantRequest requestInst = new AccessTokenGrantRequest(clientId, clientSecret, result.code(), result.state());
                Optional<AccessTokenGrantResponse> responseInst = requester.get().execute(ChzzkHttpClient.okhttp(), requestInst);

                if (responseInst.isEmpty()) {
                    log.error("CHZZK grant token failed: empty response");
                    return false;
                }

                // 토큰 바인드
                ChzzkToken token = new ChzzkToken(responseInst.get().accessToken(), responseInst.get().refreshToken());
                if (chzzk instanceof ChzzkTokenMutator mutator) {
                    mutator.setToken(token);

                    // 치지직 토큰 발급 이벤트 호출
                    if (chzzk instanceof ChzzkEventHandlerHolder holder) {
                        holder.getHandlers().forEach(handler -> handler.onGrantToken(chzzk));
                    }
                }

                // 유저 인증 성공 이벤트 호출
                if (chzzk instanceof ChzzkEventHandlerHolder holder) {
                    for (ChzzkEventHandler handler : holder.getHandlers()) {
                        if (!handler.onUserRegistered(chzzk, result.user())) return false;
                    }
                }
                return true;
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
