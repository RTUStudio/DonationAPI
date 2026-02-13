package kr.rtustudio.donation.service.chzzk.official;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkDonationMessage;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkDonationType;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkPlayer;
import kr.rtustudio.donation.service.chzzk.official.event.ChzzkEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j(topic = "ChzzkSubscriber")
public class ChzzkSubscriber implements ChzzkEventHandler {

    private final ChzzkService service;
    private final Map<String, UUID> registeredPlayers = new ConcurrentHashMap<>();

    ChzzkSubscriber(@NotNull ChzzkService service) {
        this.service = service;
    }

    @Override
    public void onUserRegistered(@NotNull Chzzk chzzk, @Nullable String user) {
        if (user == null) {
            log.warn("User is null, skipping registration");
            return;
        }

        final UUID uuid;
        try {
            uuid = UUID.fromString(user);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse UUID from user string: {}", user, e);
            return;
        }

        try {
            chzzk.getCurrentChannel().ifPresent(channel ->
                    chzzk.getToken().ifPresent(token -> {
                        registeredPlayers.put(channel.id(), uuid);
                        Consumer<ChzzkPlayer> handler = service.getRegisterHandler();
                        if (handler != null) handler.accept(new ChzzkPlayer(uuid, channel.id(), token));
                    })
            );
        } catch (Exception e) {
            log.error("Failed to get current channel for player {}", uuid, e);
        }

        chzzk.getSession().connectAsync().exceptionally(throwable -> {
            log.error("Failed to connect session for player {}", uuid, throwable);
            return null;
        });
    }

    @Override
    public void onDonationMessage(@NotNull Chzzk chzzk, @NotNull ChzzkDonationMessage message) {
        if (message.donationType() != ChzzkDonationType.CHAT) return;
        UUID uuid = registeredPlayers.get(message.receiverChannelId());
        Donation donation = new Donation(
                uuid,
                service.getType(),
                Platform.CHZZK,
                DonationType.CHAT,
                message.receiverChannelId(),
                message.senderChannelId(),
                message.nickname(),
                message.message(),
                message.payAmount()
        );
        if (service.getDonationHandler() != null) service.getDonationHandler().accept(donation);
    }
}
