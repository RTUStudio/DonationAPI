package kr.rtustudio.donation.service.cime;

import kr.rtustudio.donation.service.AbstractService;
import kr.rtustudio.donation.service.Disconnectable;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.cime.configuration.CimeConfig;
import kr.rtustudio.donation.service.cime.data.CimePlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Slf4j(topic = "DonationAPI/CIME")
public class CimeService extends AbstractService<CimePlayer> implements Disconnectable {

    @Getter
    private final CimeConfig config;
    private CimeSubscriber subscriber;

    public CimeService(CimeConfig config, ServiceHandler<CimePlayer> handler) {
        super(handler);
        this.config = config;
    }

    @Override
    public Services getType() {
        return Services.CIME;
    }

    @Override
    public void start() {
        if (!config.isEnabled()) return;
        this.subscriber = new CimeSubscriber(this);
        log.debug("CIME service started");
    }

    public boolean reconnect(@NotNull UUID uuid, @NotNull CimePlayer player) {
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
