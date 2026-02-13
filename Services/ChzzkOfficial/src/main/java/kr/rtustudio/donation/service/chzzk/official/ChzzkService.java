package kr.rtustudio.donation.service.chzzk.official;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.service.AbstractService;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.chzzk.official.configuration.ChzzkConfig;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkPlayer;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkToken;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.function.Consumer;

@Slf4j(topic = "DonationAPI/ChzzkOfficial")
public class ChzzkService extends AbstractService<ChzzkPlayer> {

    @Getter
    private final ChzzkConfig config;
    private ChzzkSubscriber subscriber;
    private ChzzkAuthServer authServer;

    public ChzzkService(ChzzkConfig config, Consumer<Donation> donationHandler, Consumer<ChzzkPlayer> registerHandler) {
        super(donationHandler, registerHandler);
        this.config = config;
    }

    @Override
    public Services getType() {
        return Services.ChzzkOfficial;
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
    public void close() {
        if (authServer != null) {
            authServer.stop();
            authServer = null;
        }
    }
}
