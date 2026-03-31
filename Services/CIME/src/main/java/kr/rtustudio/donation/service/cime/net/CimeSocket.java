package kr.rtustudio.donation.service.cime.net;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.common.net.WebSocketClient;
import lombok.extern.slf4j.Slf4j;
import okhttp3.WebSocket;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "DonationAPI/CIME")
public class CimeSocket extends WebSocketClient {
    
    private static final String WS_BASE_URL = "wss://apigw.prod.ci.me/";
    private static final String ALERT_TYPE = "DONATION_CHAT";

    private final DonationCallback donationCallback;
    private final Set<UUID> subscribers = ConcurrentHashMap.newKeySet();
    private volatile boolean connected = false;

    @FunctionalInterface
    public interface DonationCallback {
        void onDonation(int amount, String nickname, String message, boolean isAnonymous);
    }

    public CimeSocket(
            String alertKey,
            SocketOption socketOption,
            DonationCallback donationCallback,
            Runnable successCallback,
            Runnable failureCallback
    ) {
        super("Cime-" + alertKey,
                WS_BASE_URL + "?type=ALERT_KEY&alertKey=" + alertKey + "&alertType=" + ALERT_TYPE,
                socketOption,
                () -> {
                    // CimeSocket 내부에서 connected 속성 지정 후 외부 successCallback 호출
                    if (successCallback != null) successCallback.run();
                },
                failureCallback);
        this.donationCallback = donationCallback;
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
        JsonObject initMsg = new JsonObject();
        initMsg.addProperty("type", ALERT_TYPE);
        webSocket.send(initMsg.toString());
    }

    @Override
    public void onMessage(@NotNull WebSocket webSocket, @NotNull String text) {
        try {
            JsonObject raw = JsonParser.parseString(text).getAsJsonObject();
            parseDonation(raw);
        } catch (Exception e) {
            log.debug("Failed to parse CIME message: {}", text, e);
        }
    }

    private void parseDonation(JsonObject raw) {
        JsonObject extra = null;

        if (raw.has("Attributes") && !raw.get("Attributes").isJsonNull()) {
            JsonObject attrs = raw.getAsJsonObject("Attributes");
            if (attrs.has("extra") && !attrs.get("extra").isJsonNull()) {
                extra = JsonParser.parseString(attrs.get("extra").getAsString()).getAsJsonObject();
            }
        } else if (raw.has("amt")) {
            extra = raw;
        }

        if (extra == null) return;

        int amount = extra.has("amt") && !extra.get("amt").isJsonNull() ? extra.get("amt").getAsInt() : 0;
        if (amount <= 0) return;

        String message = extra.has("msg") && !extra.get("msg").isJsonNull() ? extra.get("msg").getAsString() : "";
        boolean isAnonymous = extra.has("anon") && !extra.get("anon").isJsonNull() && extra.get("anon").getAsBoolean();

        String nickname = "익명의 후원자";
        if (!isAnonymous && extra.has("prof") && !extra.get("prof").isJsonNull()) {
            JsonObject prof = extra.getAsJsonObject("prof");
            if (prof.has("ch") && !prof.get("ch").isJsonNull()) {
                JsonObject ch = prof.getAsJsonObject("ch");
                if (ch.has("na") && !ch.get("na").isJsonNull()) {
                    nickname = ch.get("na").getAsString();
                }
            }
        }

        donationCallback.onDonation(amount, nickname, message, isAnonymous);
    }
}
