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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "DonationAPI/SOOP")
public class SOOPSubscriber implements SOOPEventHandler {

    private final SOOPService service;
    private final Map<String, Set<UUID>> channelSubscribers = new ConcurrentHashMap<>();
    private final Map<String, SOOPChatSocket> chatSockets = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToChannel = new ConcurrentHashMap<>();

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

        String existingChannel = playerToChannel.get(uuid);
        if (existingChannel != null) {
            if (existingChannel.equals(bjId)) return true;
            disconnect(uuid);
        }

        playerToChannel.put(uuid, bjId);

        SOOPToken token = service.getTokenStore().get(bjId);
        if (token == null) {
            playerToChannel.remove(uuid);
            return false;
        }

        if (!connectChat(bjId, uuid, token)) {
            playerToChannel.remove(uuid);
            if (service.getHandler() != null && service.getHandler().failure() != null)
                service.getHandler().failure().accept(uuid);
            return false;
        }

        return true;
    }

    private boolean connectChat(@NotNull String bjId, @NotNull UUID uuid, @NotNull SOOPToken token) {
        SOOPChatSocket existing = chatSockets.get(bjId);
        if (existing != null) {
            channelSubscribers.computeIfAbsent(bjId, k -> ConcurrentHashMap.newKeySet()).add(uuid);
            if (existing.isJoined() && service.getHandler() != null && service.getHandler().success() != null) {
                service.getHandler().success().accept(new SOOPPlayer(uuid, bjId, token));
            }
            return true;
        }

        var chatInfoOpt = service.getApiClient().getChatInfo(token.accessToken());
        if (chatInfoOpt.isEmpty()) {
            log.warn("Failed to get chat info for {} - broadcast may not be live", bjId);
            return false;
        }

        var chatInfo = chatInfoOpt.get();
        SOOPChatSocket socket = new SOOPChatSocket(chatInfo, service.getConfig().getSocket(), new SOOPChatSocketHandler() {
            @Override
            public void onConnected(@NotNull SOOPChatSocket socket) {
                log.info("SOOP socket connected for bjId: {}", bjId);
            }

            @Override
            public void onJoined(@NotNull SOOPChatSocket socket) {
                log.info("Joined room for bjId: {}", bjId);
                Set<UUID> subs = channelSubscribers.get(bjId);
                if (subs != null && service.getHandler() != null && service.getHandler().success() != null) {
                    for (UUID subUuid : subs) {
                        service.getHandler().success().accept(new SOOPPlayer(subUuid, bjId, token));
                    }
                }
            }

            @Override
            public void onDonation(@NotNull SOOPChatSocket socket, @NotNull String action, @NotNull SOOPDonationMessage message) {
                onDonationMessage(bjId, message);
            }

            @Override
            public void onDisconnected(@NotNull SOOPChatSocket socket) {
                log.warn("SOOP socket disconnected for bjId: {}", bjId);
                chatSockets.remove(bjId);
            }

            @Override
            public void onError(@NotNull SOOPChatSocket socket, @NotNull Throwable error) {
                log.error("SOOP socket error for bjId: {}: {}", bjId, error.getMessage());
                chatSockets.remove(bjId);
                Set<UUID> subs = channelSubscribers.remove(bjId);
                if (subs != null && service.getHandler() != null && service.getHandler().failure() != null) {
                    for (UUID subUuid : subs) {
                        playerToChannel.remove(subUuid);
                        service.getHandler().failure().accept(subUuid);
                    }
                }
            }
        });

        channelSubscribers.computeIfAbsent(bjId, k -> ConcurrentHashMap.newKeySet()).add(uuid);

        chatSockets.put(bjId, socket);
        socket.connect();
        return true;
    }

    @Override
    public void onDonationMessage(@NotNull String bjId, @NotNull SOOPDonationMessage message) {
        Set<UUID> subs = channelSubscribers.get(bjId);
        if (subs == null || subs.isEmpty()) return;

        if (service.getHandler() != null && service.getHandler().donation() != null) {
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
                service.getHandler().donation().accept(donation);
            }
        }
    }

    public void disconnect(@NotNull UUID uuid) {
        String bjId = playerToChannel.remove(uuid);
        if (bjId != null) {
            Set<UUID> subs = channelSubscribers.get(bjId);
            if (subs != null) {
                subs.remove(uuid);
                if (subs.isEmpty()) {
                    channelSubscribers.remove(bjId);
                    SOOPChatSocket socket = chatSockets.remove(bjId);
                    if (socket != null) {
                        try {
                            socket.disconnect();
                        } catch (Exception e) {
                            log.warn("Failed to disconnect chat socket for player {}", uuid, e);
                        }
                    }
                }
            }
        }
    }

    public void closeAll() {
        chatSockets.values().forEach(SOOPChatSocket::disconnect);
        chatSockets.clear();
        channelSubscribers.clear();
        playerToChannel.clear();
    }
}
