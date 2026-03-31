package kr.rtustudio.donation.service.toonation;

import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.toonation.data.ToonationPlayer;
import kr.rtustudio.donation.service.toonation.net.ToonationSocket;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "DonationAPI/Toonation")
public class ToonationSubscriber {

    private final ToonationService service;
    private final Map<String, ToonationSocket> activeSockets = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToAlertKey = new ConcurrentHashMap<>();

    public ToonationSubscriber(ToonationService service) {
        this.service = service;
    }

    public boolean register(UUID uuid, String alertKey) {
        String existingKey = playerToAlertKey.get(uuid);
        if (existingKey != null) {
            if (existingKey.equals(alertKey)) return true;
            disconnect(uuid);
        }

        playerToAlertKey.put(uuid, alertKey);

        ServiceHandler<ToonationPlayer> handler = service.getHandler();

        ToonationSocket existingSocket = activeSockets.get(alertKey);
        if (existingSocket != null) {
            existingSocket.addSubscriber(uuid);
            if (handler.messenger() != null) handler.messenger().send(uuid, "connection.trying", null);
            if (existingSocket.isConnected()) {
                if (handler.success() != null) handler.success().accept(new ToonationPlayer(uuid, alertKey, existingSocket.getPayload()));
                if (handler.messenger() != null) handler.messenger().send(uuid, "connection.activated", null);
            }
            return true;
        }

        if (handler.messenger() != null) handler.messenger().send(uuid, "connection.trying", null);

        CompletableFuture.supplyAsync(() -> buildPayload(alertKey))
                .thenAccept(payload -> {
                    if (payload == null) {
                        log.warn("Failed to find Toonation payload for key: {}", alertKey);
                        playerToAlertKey.remove(uuid);
                        return;
                    }

                    ToonationSocket socket = activeSockets.computeIfAbsent(alertKey, key -> {
                        ToonationSocket newSocket = new ToonationSocket(
                                service, key, payload,
                                () -> {
                                    ToonationSocket s = activeSockets.get(key);
                                    if (s == null) return;
                                    for (UUID subUuid : s.getSubscribers()) {
                                        if (handler.success() != null) handler.success().accept(new ToonationPlayer(subUuid, key, payload));
                                        if (handler.messenger() != null) handler.messenger().send(subUuid, "connection.activated", null);
                                    }
                                }
                        );
                        newSocket.connect();
                        return newSocket;
                    });

                    socket.addSubscriber(uuid);

                    if (socket.isConnected()) {
                        if (handler.success() != null) handler.success().accept(new ToonationPlayer(uuid, alertKey, payload));
                        if (handler.messenger() != null) handler.messenger().send(uuid, "connection.activated", null);
                    }
                })
                .exceptionally(ex -> {
                    log.error("Exception occurred while fetching Toonation payload for key: {}", alertKey, ex);
                    playerToAlertKey.remove(uuid);
                    return null;
                });

        return true;
    }

    private String buildPayload(String alertKey) {
        String json = "{\"auth\":\"" + alertKey + "\",\"service\":\"alert\",\"type\":0,\"language\":\"ko\"}";
        return Base64.getEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
    }

    public void removeClient(UUID uuid) {
        disconnect(uuid);
    }

    public void disconnect(UUID uuid) {
        String alertKey = playerToAlertKey.remove(uuid);
        if (alertKey == null) return;

        ToonationSocket socket = activeSockets.get(alertKey);
        if (socket != null) {
            socket.removeSubscriber(uuid);
            if (socket.getSubscribersCount() == 0) {
                socket.close();
                activeSockets.remove(alertKey);
            }
        }
    }

    public void closeAll() {
        activeSockets.values().forEach(ToonationSocket::close);
        activeSockets.clear();
        playerToAlertKey.clear();
    }
}
