package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.service.AbstractService;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.chzzk.configuration.ChzzkConfig;
import kr.rtustudio.donation.service.chzzk.data.ChzzkPlayer;
import kr.rtustudio.donation.service.chzzk.data.ChzzkToken;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Slf4j(topic = "DonationAPI/Chzzk")
public class ChzzkService extends AbstractService<ChzzkPlayer> implements kr.rtustudio.donation.service.Disconnectable {

    @Getter
    private final ChzzkConfig config;
    private ChzzkSubscriber subscriber;
    private ChzzkAuthServer authServer;

    public ChzzkService(ChzzkConfig config, ServiceHandler<ChzzkPlayer> handler) {
        super(handler);
        this.config = config;
    }

    @Override
    public Services getType() {
        return Services.Chzzk;
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
        } catch (Exception e) {
            log.error("Failed to refresh token for player {}, token may be expired", uuid, e);
            return false;
        }

        subscriber.onUserRegistered(chzzk, uuid.toString());
        return true;
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
