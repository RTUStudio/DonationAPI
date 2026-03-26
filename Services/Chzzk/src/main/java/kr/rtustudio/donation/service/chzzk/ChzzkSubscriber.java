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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "DonationAPI/Chzzk")
public class ChzzkSubscriber implements ChzzkEventHandler {

    private final ChzzkService service;
    private final Map<String, Set<UUID>> channelSubscribers = new ConcurrentHashMap<>();
    private final Map<String, Chzzk> activeSessions = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToChannel = new ConcurrentHashMap<>();

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

                String existingChannel = playerToChannel.get(uuid);
                if (existingChannel != null) {
                    if (existingChannel.equals(channelId)) return true;
                    disconnect(uuid);
                }

                playerToChannel.put(uuid, channelId);

                Chzzk existing = activeSessions.get(channelId);
                if (existing != null && existing.getSession().isConnected()) {
                    channelSubscribers.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet()).add(uuid);
                    if (service.getHandler() != null && service.getHandler().success() != null) {
                        service.getHandler().success().accept(new ChzzkPlayer(uuid, channelId, tokenOpt.get()));
                    }
                    return true;
                }

                channelSubscribers.computeIfAbsent(channelId, k -> ConcurrentHashMap.newKeySet()).add(uuid);
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
                    Set<UUID> subs = channelSubscribers.get(finalChannelId);
                    if (subs != null && service.getHandler() != null && service.getHandler().success() != null) {
                        for (UUID subUuid : subs) {
                            service.getHandler().success().accept(new ChzzkPlayer(subUuid, finalChannelId, tokenOpt.get()));
                        }
                    }
                })
                .exceptionally(throwable -> {
                    log.error("Failed to connect session for channel {}", finalChannelId, throwable);
                    Set<UUID> subs = channelSubscribers.remove(finalChannelId);
                    activeSessions.remove(finalChannelId);
                    if (subs != null && service.getHandler() != null && service.getHandler().failure() != null) {
                        for (UUID subUuid : subs) {
                            playerToChannel.remove(subUuid);
                            service.getHandler().failure().accept(subUuid);
                        }
                    }
                    return null;
                });
        return true;
    }

    public void disconnect(@NotNull UUID uuid) {
        String channelId = playerToChannel.remove(uuid);
        if (channelId != null) {
            Set<UUID> subs = channelSubscribers.get(channelId);
            if (subs != null) {
                subs.remove(uuid);
                if (subs.isEmpty()) {
                    channelSubscribers.remove(channelId);
                    Chzzk chzzk = activeSessions.remove(channelId);
                    if (chzzk != null && chzzk.getSession().isConnected()) {
                        try {
                            chzzk.getSession().disconnect();
                        } catch (Exception e) {
                            log.warn("Failed to disconnect session for channel {}", channelId, e);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onDonationMessage(@NotNull Chzzk chzzk, @NotNull ChzzkDonationMessage message) {
        if (message.donationType() != ChzzkDonationType.CHAT) return;
        
        Set<UUID> subs = channelSubscribers.get(message.receiverChannelId());
        if (subs == null || subs.isEmpty()) return;

        if (service.getHandler() != null && service.getHandler().donation() != null) {
            for (UUID uuid : subs) {
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
                service.getHandler().donation().accept(donation);
            }
        }
    }

    public void closeAll() {
        activeSessions.values().forEach(chzzk -> {
            if (chzzk.getSession().isConnected()) {
                try {
                    chzzk.getSession().disconnect();
                } catch (Exception e) {
                    log.warn("Failed to disconnect session", e);
                }
            }
        });
        activeSessions.clear();
        channelSubscribers.clear();
        playerToChannel.clear();
    }
}
