package kr.rtustudio.donation.service.toonation;

import kr.rtustudio.donation.service.AbstractService;
import kr.rtustudio.donation.service.Disconnectable;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.toonation.configuration.ToonationConfig;
import kr.rtustudio.donation.service.toonation.data.ToonationPlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Slf4j(topic = "DonationAPI/Toonation")
public class ToonationService extends AbstractService<ToonationPlayer> implements Disconnectable {

    @Getter
    private final ToonationConfig config;
    private ToonationSubscriber subscriber;

    public ToonationService(ToonationConfig config, ServiceHandler<ToonationPlayer> handler) {
        super(handler);
        this.config = config;
    }

    @Override
    public Services getType() {
        return Services.Toonation;
    }

    @Override
    public void start() {
        if (!config.isEnabled()) return;
        this.subscriber = new ToonationSubscriber(this);
        log.debug("Toonation service started");
    }

    public boolean reconnect(@NotNull UUID uuid, @NotNull ToonationPlayer player) {
        if (subscriber == null) {
            log.warn("Cannot reconnect: service not started");
            return false;
        }
        return subscriber.register(uuid, player.alertKey());
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
    }
}
