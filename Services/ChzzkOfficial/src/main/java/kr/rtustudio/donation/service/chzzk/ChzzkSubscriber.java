package kr.rtustudio.donation.service.chzzk;

import com.google.common.collect.Maps;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationMessage;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationType;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEvent;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.Flow;

@Slf4j(topic = "ChzzkSubscriber")
@RequiredArgsConstructor
public class ChzzkSubscriber implements Flow.Subscriber<ChzzkEvent<?>> {

    private final ChzzkService service;

    private Flow.Subscription subscription;
    private Map<String, Chzzk> chzzkMap = Maps.newConcurrentMap();

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        this.subscription.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(ChzzkEvent<?> item) {
        if (item.type() == ChzzkEventType.USER_REGISTERED) {
            Chzzk chzzk = item.source();
//            chzzk.getCurrentChannel().thenAccept(opt -> {
//                opt.ifPresent(channel -> {
//                    String channelUUID = channel.id();
//                    chzzkMap.put(channelUUID, chzzk);
//
//                    chzzk.getToken().ifPresent(token -> {
//                        BukkitDonationAPI.getInstance().getStorage().add("Key",
//                                JSON.of("id", channelUUID).append("accessToken", token.accessToken())
//                                        .append("refreshToken", token.refreshToken()));
//                    });
//                });
//            });

//            ChzzkToken token = chzzk.getToken().get();
//            token.accessToken();
//            token.refreshToken();
////
//
//            Chzzk other = Chzzk.builder()
//                    .token(token)
//                    .clientId(config.getClientId())
//                    .clientSecret(config.getClientSecret())
//                    .baseUri(config.getBaseUri())
//                    .host(config.getHost())
//                    .port(config.getPort())
//                    .build();
//
//            other.refreshToken().thenRun(() -> {
//
//            });

            ChzzkSession session = chzzk.getSession();
            session.connectAsync();
            return;
        }
        if (item.type() == ChzzkEventType.DONATION_MESSAGE) {
            if (item.payload() instanceof ChzzkDonationMessage value) {
                if (value.donationType() != ChzzkDonationType.CHAT) return;
                Donation donation = new Donation(
                        service.getType(),
                        Platform.CHZZK,
                        DonationType.CHAT,
                        value.receiverChannelId(),
                        value.senderChannelId(),
                        value.nickname(),
                        value.message(),
                        value.payAmount()
                );
                if (service.getDonationHandler() != null) {
                    service.getDonationHandler().accept(donation);
                }
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        log.error("ChzzkOfficial event stream error", throwable);
    }

    @Override
    public void onComplete() {
        if (subscription != null) subscription.cancel();
    }
}
