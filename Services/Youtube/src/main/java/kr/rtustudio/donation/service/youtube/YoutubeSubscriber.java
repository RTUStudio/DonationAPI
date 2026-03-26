package kr.rtustudio.donation.service.youtube;

import kr.rtustudio.donation.service.youtube.net.YoutubeClient;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "DonationAPI/Youtube")
public class YoutubeSubscriber {

    private final YoutubeService service;
    private final Map<String, YoutubeClient> activeClients = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToHandle = new ConcurrentHashMap<>();

    public YoutubeSubscriber(YoutubeService service) {
        this.service = service;
    }

    public boolean register(UUID uuid, String handle) {
        String existingHandle = playerToHandle.get(uuid);
        if (existingHandle != null) {
            if (existingHandle.equals(handle)) return true;
            disconnect(uuid);
        }

        playerToHandle.put(uuid, handle);
        YoutubeClient client = activeClients.computeIfAbsent(handle, h -> {
            try {
                YoutubeClient newClient = new YoutubeClient(service, h, service.getConfig().getPollingIntervalMs());
                newClient.start();
                log.info("Started new YouTube polling task for handle: {}", h);
                return newClient;
            } catch (Exception e) {
                log.error("Failed to connect to YouTube Live Chat for user: {} ({})", h, e.getMessage());
                return null;
            }
        });

        if (client == null) {
            playerToHandle.remove(uuid);
            return false;
        }

        client.addSubscriber(uuid);
        log.info("Registered YouTube subscriber for Handle: {} (UUID: {})", handle, uuid);
        return true;
    }

    public void disconnect(UUID uuid) {
        String handle = playerToHandle.remove(uuid);
        if (handle != null) {
            YoutubeClient client = activeClients.get(handle);
            if (client != null) {
                client.removeSubscriber(uuid);
                if (client.getSubscribersCount() == 0) {
                    client.close();
                    activeClients.remove(handle);
                    log.info("Stopped YouTube polling task for handle: {} (no more subscribers)", handle);
                }
            }
            log.info("Disconnected YouTube subscriber for UUID: {}", uuid);
        }
    }

    public void closeAll() {
        activeClients.values().forEach(YoutubeClient::close);
        activeClients.clear();
        playerToHandle.clear();
        log.info("Closed all YouTube subscribers");
    }
}
