package kr.rtustudio.donation.service.toonation.net;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.common.net.WebSocketClient;
import kr.rtustudio.donation.service.toonation.ToonationService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "DonationAPI/Toonation")
public class ToonationSocket extends WebSocketClient {

    private static final Gson GSON = new Gson();

    private final ToonationService service;
    private final String payload;
    private final Set<UUID> subscribers = ConcurrentHashMap.newKeySet();
    private volatile boolean connected = false;

    public ToonationSocket(ToonationService service, String alertKey, String payload, Runnable successCallback) {
        super("Toonation-" + (alertKey.length() > 8 ? alertKey.substring(0, 8) : alertKey),
                "wss://ws.toon.at/" + payload,
                service.getConfig().getSocket(),
                () -> {
                    if (successCallback != null) successCallback.run();
                },
                null);
        this.service = service;
        this.payload = payload;
    }

    @Override
    protected void configureRequest(okhttp3.Request.Builder builder) {
        builder.header("Origin", "https://toon.at");
    }

    public String getPayload() {
        return payload;
    }

    public void addSubscriber(UUID uuid) {
        subscribers.add(uuid);
    }

    public void removeSubscriber(UUID uuid) {
        subscribers.remove(uuid);
    }

    public int getSubscribersCount() {
        return subscribers.size();
    }

    public Set<UUID> getSubscribers() {
        return subscribers;
    }

    public boolean isConnected() {
        return connected;
    }

    @Override
    protected void handleOpen(WebSocket webSocket) {
        connected = true;
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        try {
            JsonObject json = GSON.fromJson(text, JsonObject.class);
            if (!json.has("content")) return;

            JsonObject content = json.getAsJsonObject("content");
            if (content == null) return;

            String id = content.has("account") ? content.get("account").getAsString() : "unknown";
            String name = content.has("name") ? content.get("name").getAsString() : "익명";
            long amount = content.has("amount") ? content.get("amount").getAsLong() : 0L;
            String comment = content.has("message") ? content.get("message").getAsString() : "";

            if (service.getHandler().donation() != null) {
                for (UUID subUuid : subscribers) {
                    Donation donation = new Donation(
                            subUuid,
                            service.getType(),
                            Platform.TOONATION,
                            DonationType.CHAT,
                            "",
                            id,
                            name,
                            comment,
                            (int) amount
                    );
                    service.getHandler().donation().accept(donation);
                }
            }

        } catch (Exception e) {
            log.warn("Failed to parse Toonation message: {}", text, e);
        }
    }
}
