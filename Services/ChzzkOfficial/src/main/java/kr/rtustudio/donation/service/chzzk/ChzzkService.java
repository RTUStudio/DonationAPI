package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.service.Service;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.chzzk.configuration.ChzzkConfig;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventStream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Consumer;

@Slf4j(topic = "DonationAPI/Chzzk")
@RequiredArgsConstructor
public class ChzzkService implements Service {

    private final ChzzkSubscriber subscriber = new ChzzkSubscriber(this);
    private final ChzzkConfig config;
    @Getter
    private Consumer<Donation> donationHandler;
    private ChzzkAuthServer authServer;
    private ChzzkEventStream eventStream;

    @Override
    public Services getType() {
        return Services.CHZZK;
    }

    @Override
    public void start(Consumer<Donation> donationHandler) {
        if (!config.isEnabled()) return;
        this.donationHandler = donationHandler;
        this.eventStream = new ChzzkEventStream();
        this.authServer = ChzzkAuthServer.builder()
                .clientId(config.getClientId())
                .clientSecret(config.getClientSecret())
                .baseUri(config.getBaseUri())
                .host(config.getHost())
                .port(config.getPort())
                .eventHandler(eventStream)
                .build();
        eventStream.publisher().subscribe(subscriber);
        authServer.start();
        log.info("ChzzkOfficial auth server started ({}:{})", config.getHost(), config.getPort());
    }

    @Override
    public void close() {
        if (authServer != null) {
            authServer.stop();
            authServer = null;
        }
        if (eventStream != null) {
            eventStream.close();
            eventStream = null;
        }
    }
}
