package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.service.AbstractService;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.chzzk.configuration.ChzzkConfig;
import kr.rtustudio.donation.service.chzzk.data.ChzzkPlayer;
import kr.rtustudio.donation.service.chzzk.data.ChzzkToken;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiConsumer;

@Slf4j(topic = "DonationAPI/Chzzk")
public class ChzzkService extends AbstractService<ChzzkPlayer> implements kr.rtustudio.donation.service.Disconnectable {

    @Getter
    private final ChzzkConfig config;
    private ChzzkSubscriber subscriber;
    private ChzzkAuthServer authServer;

    /**
     * 토큰 갱신 시 호출되는 콜백 (UUID, 새 ChzzkToken)
     * DB에 갱신된 토큰을 저장하는 용도
     */
    @Setter
    @Nullable
    private BiConsumer<UUID, ChzzkToken> tokenRefreshCallback;

    public ChzzkService(ChzzkConfig config, ServiceHandler<ChzzkPlayer> handler) {
        super(handler);
        this.config = config;
    }

    @Override
    public Services getType() {
        return Services.CHZZK;
    }

    @Override
    public void start() {
        if (!config.isEnabled()) return;
        this.subscriber = new ChzzkSubscriber(this);
        this.authServer = ChzzkAuthServer.builder()
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .baseUri(config.getBaseUri())
                .host(config.getHost())
                .port(config.getPort())
                .socketOption(config.getSocket())
                .addChzzkEventHandler(subscriber)
                .build();
        authServer.start();
        log.debug("CHZZK service started");
    }

    public boolean reconnect(@NotNull UUID uuid, @NotNull ChzzkToken token) {
        if (subscriber == null) {
            log.warn("Cannot reconnect: service not started");
            return false;
        }

        Chzzk chzzk = Chzzk.builder()
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .token(token)
                .socketOption(config.getSocket())
                .addEventHandler(subscriber)
                .build();

        try {
            chzzk.refreshToken();
            // 토큰 갱신 성공 시 DB에 새 토큰 저장
            chzzk.getToken().ifPresent(newToken -> {
                if (tokenRefreshCallback != null) {
                    tokenRefreshCallback.accept(uuid, newToken);
                    log.info("Updated refreshed token in storage for player {}", uuid);
                }
            });
        } catch (Exception e) {
            log.warn("Token refresh failed for player {}, trying with existing token: {}", uuid, e.getMessage());
        }

        if (chzzk.getToken().isEmpty()) {
            log.warn("Token is null after refresh for player {}", uuid);
            return false;
        }

        boolean result = subscriber.onUserRegistered(chzzk, uuid.toString());
        if (!result) {
            log.warn("Failed to register player {} after token refresh", uuid);
        }
        return result;
    }

    @Override
    public void disconnect(@NotNull UUID uuid) {
        if (subscriber != null) {
            subscriber.disconnect(uuid);
        }
    }

    @Override
    public void close() {
        if (subscriber != null) {
            subscriber.closeAll();
            subscriber = null;
        }
        if (authServer != null) {
            authServer.stop();
            authServer = null;
        }
    }
}
