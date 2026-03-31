package kr.rtustudio.donation.service.youtube;

import kr.rtustudio.donation.service.AbstractService;
import kr.rtustudio.donation.service.Disconnectable;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.youtube.configuration.YoutubeConfig;
import kr.rtustudio.donation.service.youtube.data.YoutubePlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Slf4j(topic = "DonationAPI/Youtube")
public class YoutubeService extends AbstractService<YoutubePlayer> implements Disconnectable {

    @Getter
    private final YoutubeConfig config;
    private YoutubeSubscriber subscriber;

    public YoutubeService(YoutubeConfig config, ServiceHandler<YoutubePlayer> handler) {
        super(handler);
        this.config = config;
    }

    @Override
    public Services getType() {
        return Services.Youtube;
    }

    @Override
    public void start() {
        if (!config.isEnabled()) return;
        this.subscriber = new YoutubeSubscriber(this);
        log.debug("Youtube service started");
    }

    public boolean reconnect(@NotNull UUID uuid, @NotNull YoutubePlayer player) {
        if (subscriber == null) {
            log.warn("Cannot reconnect: service not started");
            return false;
        }
        return subscriber.register(uuid, player.channelId());
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
