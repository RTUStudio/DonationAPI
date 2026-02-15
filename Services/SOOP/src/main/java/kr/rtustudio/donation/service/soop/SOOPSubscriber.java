package kr.rtustudio.donation.service.soop;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.soop.data.SOOPDonationMessage;
import kr.rtustudio.donation.service.soop.data.SOOPPlayer;
import kr.rtustudio.donation.service.soop.data.SOOPToken;
import kr.rtustudio.donation.service.soop.event.SOOPEventHandler;
import kr.rtustudio.donation.service.soop.net.socket.SOOPChatSocket;
import kr.rtustudio.donation.service.soop.net.socket.SOOPChatSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Slf4j(topic = "DonationAPI/SOOP")
public class SOOPSubscriber implements SOOPEventHandler {

    private final SOOPService service;
    private final Map<String, UUID> registeredPlayers = new ConcurrentHashMap<>();
    private final Map<String, SOOPChatSocket> chatSockets = new ConcurrentHashMap<>();

    SOOPSubscriber(@NotNull SOOPService service) {
        this.service = service;
    }

    @Override
    public boolean onUserRegistered(@NotNull String bjId, @Nullable String user) {
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

        SOOPToken token = service.getTokenStore().get(bjId);
        if (token == null) return false;

        if (!connectChat(bjId, uuid, token)) {
            if (service.getHandler() != null && service.getHandler().failure() != null)
                service.getHandler().failure().accept(uuid);
            return false;
        }

        return true;
    }

    private boolean connectChat(@NotNull String bjId, @NotNull UUID uuid, @NotNull SOOPToken token) {
        // 기존 소켓이 있으면 disconnect
        SOOPChatSocket existing = chatSockets.remove(bjId);
        if (existing != null) {
            try {
                existing.disconnect();
            } catch (Exception e) {
                log.warn("Failed to disconnect existing chat socket for {}", bjId, e);
            }
        }

        var chatInfoOpt = service.getApiClient().getChatInfo(token.accessToken());
        if (chatInfoOpt.isEmpty()) {
            log.warn("Failed to get chat info for {} - broadcast may not be live", bjId);
            return false;
        }

        var chatInfo = chatInfoOpt.get();
        SOOPChatSocket socket = new SOOPChatSocket(chatInfo, new SOOPChatSocketHandler() {
            @Override
            public void onConnected(@NotNull SOOPChatSocket socket) {
                log.info("Chat connected for {}", bjId);
            }

            @Override
            public void onJoined(@NotNull SOOPChatSocket socket) {
                log.info("Chat joined for {}", bjId);
                // 채널 조인 성공 시에만 등록 및 success 핸들러 호출
                registeredPlayers.put(bjId, uuid);
                if (service.getHandler() != null && service.getHandler().success() != null)
                    service.getHandler().success().accept(new SOOPPlayer(uuid, bjId, token));
            }

            @Override
            public void onDonation(@NotNull SOOPChatSocket socket, @NotNull String action, @NotNull SOOPDonationMessage message) {
                onDonationMessage(bjId, message);
            }

            @Override
            public void onDisconnected(@NotNull SOOPChatSocket socket) {
                log.warn("Chat disconnected for {}", bjId);
                chatSockets.remove(bjId);
                registeredPlayers.remove(bjId);
            }

            @Override
            public void onError(@NotNull SOOPChatSocket socket, @NotNull Throwable error) {
                log.error("Chat error for {}: {}", bjId, error.getMessage());
                chatSockets.remove(bjId);
                registeredPlayers.remove(bjId);
                if (service.getHandler() != null && service.getHandler().failure() != null)
                    service.getHandler().failure().accept(uuid);
            }
        });

        chatSockets.put(bjId, socket);
        socket.connect();
        return true;
    }

    @Override
    public void onDonationMessage(@NotNull String bjId, @NotNull SOOPDonationMessage message) {
        UUID uuid = registeredPlayers.get(bjId);
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
        if (service.getHandler() != null && service.getHandler().donation() != null)
            service.getHandler().donation().accept(donation);
    }

    public void disconnect(@NotNull UUID uuid) {
        registeredPlayers.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(uuid)) {
                SOOPChatSocket socket = chatSockets.remove(entry.getKey());
                if (socket != null) {
                    try {
                        socket.disconnect();
                    } catch (Exception e) {
                        log.warn("Failed to disconnect chat socket for player {}", uuid, e);
                    }
                }
                return true;
            }
            return false;
        });
    }

    public void disconnectAll() {
        chatSockets.values().forEach(SOOPChatSocket::disconnect);
        chatSockets.clear();
        registeredPlayers.clear();
    }
}
