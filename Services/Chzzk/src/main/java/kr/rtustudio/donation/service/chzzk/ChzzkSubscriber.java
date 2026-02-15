package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationMessage;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationType;
import kr.rtustudio.donation.service.chzzk.data.ChzzkPlayer;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "ChzzkSubscriber")
public class ChzzkSubscriber implements ChzzkEventHandler {

    private final ChzzkService service;
    private final Map<String, UUID> registeredPlayers = new ConcurrentHashMap<>();
    private final Map<String, Chzzk> activeSessions = new ConcurrentHashMap<>();

    ChzzkSubscriber(@NotNull ChzzkService service) {
        this.service = service;
    }

    @Override
    public boolean onUserRegistered(@NotNull Chzzk chzzk, @Nullable String user) {
        if (user == null) {
            log.warn("User is null, skipping registration");
            return false;
        }

        final UUID uuid;
        try {
            uuid = UUID.fromString(user);
        } catch (IllegalArgumentException e) {
            log.error("Failed to parse UUID from user string: {}", user, e);
            return false;
        }

        boolean registered = false;
        String channelId = null;
        try {
            var channelOpt = chzzk.getCurrentChannel();
            var tokenOpt = chzzk.getToken();
            if (channelOpt.isPresent() && tokenOpt.isPresent()) {
                channelId = channelOpt.get().id();

                // 기존 세션이 있으면 disconnect
                Chzzk existing = activeSessions.remove(channelId);
                if (existing != null && existing.getSession().isConnected()) {
                    try {
                        existing.getSession().disconnect();
                    } catch (Exception e) {
                        log.warn("Failed to disconnect existing session for channel {}", channelId, e);
                    }
                }

                registeredPlayers.put(channelId, uuid);
                activeSessions.put(channelId, chzzk);
                registered = true;
            }
        } catch (Exception e) {
            log.error("Failed to get current channel for player {}", uuid, e);
        }

        if (!registered) {
            if (service.getHandler() != null && service.getHandler().failure() != null)
                service.getHandler().failure().accept(uuid);
            return false;
        }

        var tokenOpt = chzzk.getToken();
        var channelOpt = chzzk.getCurrentChannel();
        if (tokenOpt.isEmpty() || channelOpt.isEmpty()) {
            if (service.getHandler() != null && service.getHandler().failure() != null)
                service.getHandler().failure().accept(uuid);
            return false;
        }

        String finalChannelId = channelOpt.get().id();
        chzzk.getSession().connectAsync()
                .thenAccept(v -> {
                    // 연결 성공 시에만 success 핸들러 호출
                    if (service.getHandler() != null && service.getHandler().success() != null)
                        service.getHandler().success().accept(new ChzzkPlayer(uuid, finalChannelId, tokenOpt.get()));
                })
                .exceptionally(throwable -> {
                    log.error("Failed to connect session for player {}", uuid, throwable);
                    registeredPlayers.remove(finalChannelId);
                    activeSessions.remove(finalChannelId);
                    if (service.getHandler() != null && service.getHandler().failure() != null)
                        service.getHandler().failure().accept(uuid);
                    return null;
                });
        return true;
    }

    public void disconnect(@NotNull UUID uuid) {
        registeredPlayers.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(uuid)) {
                Chzzk chzzk = activeSessions.remove(entry.getKey());
                if (chzzk != null && chzzk.getSession().isConnected()) {
                    try {
                        chzzk.getSession().disconnect();
                    } catch (Exception e) {
                        log.warn("Failed to disconnect session for player {}", uuid, e);
                    }
                }
                return true;
            }
            return false;
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
        if (service.getHandler() != null && service.getHandler().donation() != null)
            service.getHandler().donation().accept(donation);
    }
}
