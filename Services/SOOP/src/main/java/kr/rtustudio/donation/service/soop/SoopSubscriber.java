package kr.rtustudio.donation.service.soop;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.soop.data.SoopDonationMessage;
import kr.rtustudio.donation.service.soop.data.SoopPlayer;
import kr.rtustudio.donation.service.soop.data.SoopToken;
import kr.rtustudio.donation.service.soop.event.SoopEventHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "DonationAPI/SOOP")
public class SoopSubscriber implements SoopEventHandler {

    private final SoopService service;
    private final Map<String, Set<UUID>> channelSubscribers = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToChannel = new ConcurrentHashMap<>();
    private final Map<String, SoopToken> registeredTokens = new ConcurrentHashMap<>();

    SoopSubscriber(@NotNull SoopService service) {
        this.service = service;
    }

    @Override
    public boolean onUserRegistered(@NotNull String bjId, @Nullable String user) {
        return onUserRegistered(bjId, user, bjId);
    }

    public boolean onUserRegistered(@NotNull String bjId, @Nullable String user, @NotNull String stationName) {
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

        String existingChannel = playerToChannel.get(uuid);
        if (existingChannel != null) {
            if (existingChannel.equals(bjId)) return true;
            disconnect(uuid);
        }

        SoopToken token = service.getTokenStore().get(bjId);
        if (token == null) {
            playerToChannel.remove(uuid);
            return false;
        }

        playerToChannel.put(uuid, bjId);
        channelSubscribers.computeIfAbsent(bjId, k -> ConcurrentHashMap.newKeySet()).add(uuid);
        registeredTokens.put(bjId, token);

        ServiceHandler<SoopPlayer> handler = service.getHandler();
        if (handler.success() != null) handler.success().accept(new SoopPlayer(uuid, bjId, token));

        if (handler.messenger() != null) {
            handler.messenger().send(uuid, "connection.trying", stationName);
            SoopLiveMonitor monitor = service.getLiveMonitor();
            if (monitor != null && monitor.hasActiveSocket(bjId)) {
                handler.messenger().send(uuid, "connection.activated", null);
            } else {
                handler.messenger().send(uuid, "connection.waiting", null);
                if (monitor != null) monitor.checkImmediately(bjId);
            }
        }

        return true;
    }

    void onSocketJoined(@NotNull String bjId, @NotNull SoopToken token) {
        Set<UUID> subs = channelSubscribers.get(bjId);
        if (subs == null || subs.isEmpty()) return;

        ServiceHandler<SoopPlayer> handler = service.getHandler();
        if (handler.messenger() != null) {
            for (UUID uuid : subs) {
                handler.messenger().send(uuid, "connection.activated", null);
            }
        }
    }

    void onSocketDisconnected(@NotNull String bjId) {
        // 연동 자체는 유지, 방송 종료만 기록
    }

    @Override
    public void onDonationMessage(@NotNull String bjId, @NotNull SoopDonationMessage message) {
        Set<UUID> subs = channelSubscribers.get(bjId);
        if (subs == null || subs.isEmpty()) return;

        ServiceHandler<SoopPlayer> handler = service.getHandler();
        if (handler.donation() == null) return;

        for (UUID uuid : subs) {
            Donation donation = new Donation(
                    uuid,
                    service.getType(),
                    Platform.SOOP,
                    DonationType.CHAT,
                    message.bjId(),
                    message.userId(),
                    message.userNickname(),
                    message.message(),
                    message.count()
            );
            handler.donation().accept(donation);
        }
    }

    @NotNull Map<String, SoopToken> getRegisteredTokens() {
        return registeredTokens;
    }

    public void disconnect(@NotNull UUID uuid) {
        String bjId = playerToChannel.remove(uuid);
        if (bjId == null) return;

        Set<UUID> subs = channelSubscribers.get(bjId);
        if (subs != null) {
            subs.remove(uuid);
            if (subs.isEmpty()) {
                channelSubscribers.remove(bjId);
                registeredTokens.remove(bjId);
                SoopLiveMonitor monitor = service.getLiveMonitor();
                if (monitor != null) monitor.disconnectSocket(bjId);
            }
        }
    }

    public void closeAll() {
        channelSubscribers.clear();
        playerToChannel.clear();
        registeredTokens.clear();
    }
}
