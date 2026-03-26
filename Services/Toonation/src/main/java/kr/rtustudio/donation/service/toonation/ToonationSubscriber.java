package kr.rtustudio.donation.service.toonation;

import kr.rtustudio.donation.service.toonation.data.ToonationPlayer;
import kr.rtustudio.donation.service.toonation.net.ToonationSocket;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j(topic = "DonationAPI/Toonation")
public class ToonationSubscriber {

    private final ToonationService service;
    private final Map<String, ToonationSocket> activeSockets = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToAlertKey = new ConcurrentHashMap<>();
    private final Pattern payloadPattern = Pattern.compile("\"payload\":\"(.*)\",");

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

        ToonationSocket existingSocket = activeSockets.get(alertKey);
        if (existingSocket != null) {
            existingSocket.addSubscriber(uuid);
            if (existingSocket.isConnected() && service.getHandler().success() != null) {
                service.getHandler().success().accept(new ToonationPlayer(uuid, alertKey, alertKey, existingSocket.getPayload()));
            }
            log.info("Registered Toonation subscriber for alertKey: {} (UUID: {})", alertKey, uuid);
            return true;
        }

        CompletableFuture.supplyAsync(() -> fetchPayload(alertKey))
                .thenAccept(payload -> {
                    if (payload == null) {
                        log.warn("Failed to find Toonation payload for key: {}", alertKey);
                        playerToAlertKey.remove(uuid);
                        return;
                    }

                    ToonationSocket socket = activeSockets.computeIfAbsent(alertKey, key -> {
                        ToonationSocket newSocket = new ToonationSocket(
                                service,
                                key,
                                payload,
                                () -> {
                                    ToonationSocket s = activeSockets.get(key);
                                    if (s != null && service.getHandler().success() != null) {
                                        for (UUID subUuid : s.getSubscribers()) {
                                            service.getHandler().success().accept(new ToonationPlayer(subUuid, key, key, payload));
                                        }
                                    }
                                }
                        );
                        newSocket.connect();
                        log.info("Started new Toonation WebSocket for alertKey: {}", key);
                        return newSocket;
                    });

                    socket.addSubscriber(uuid);
                    log.info("Registered Toonation subscriber for alertKey: {} (UUID: {})", alertKey, uuid);

                    if (socket.isConnected() && service.getHandler().success() != null) {
                        service.getHandler().success().accept(new ToonationPlayer(uuid, alertKey, alertKey, payload));
                    }
                })
                .exceptionally(ex -> {
                    log.error("Exception occurred while fetching Toonation payload for key: " + alertKey, ex);
                    playerToAlertKey.remove(uuid);
                    return null;
                });

        return true;
    }

    private String fetchPayload(String alertKey) {
        try {
            Document doc = Jsoup.connect("https://toon.at/widget/alertbox/" + alertKey).get();
            Elements scriptElements = doc.getElementsByTag("script");
            String script = scriptElements.stream()
                    .filter(e -> !e.hasAttr("src"))
                    .map(Element::toString)
                    .collect(Collectors.joining());

            Matcher m = payloadPattern.matcher(script);
            if (m.find()) {
                return m.group(1);
            }
        } catch (Exception e) {
            log.error("Failed to parse Toonation payload", e);
        }
        return null;
    }

    public void removeClient(UUID uuid) {
        disconnect(uuid);
    }

    public void disconnect(UUID uuid) {
        String alertKey = playerToAlertKey.remove(uuid);
        if (alertKey != null) {
            ToonationSocket socket = activeSockets.get(alertKey);
            if (socket != null) {
                socket.removeSubscriber(uuid);
                if (socket.getSubscribersCount() == 0) {
                    socket.close();
                    activeSockets.remove(alertKey);
                    log.info("Stopped Toonation WebSocket for alertKey: {} (no more subscribers)", alertKey);
                }
            }
            log.info("Disconnected Toonation subscriber for UUID: {}", uuid);
        }
    }

    public void closeAll() {
        activeSockets.values().forEach(ToonationSocket::close);
        activeSockets.clear();
        playerToAlertKey.clear();
    }
}
