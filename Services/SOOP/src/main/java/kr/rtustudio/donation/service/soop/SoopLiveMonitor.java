package kr.rtustudio.donation.service.soop;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.rtustudio.donation.service.soop.data.SoopDonationMessage;
import kr.rtustudio.donation.service.soop.data.SoopToken;
import kr.rtustudio.donation.service.soop.net.data.ChatInfoResponse;
import kr.rtustudio.donation.service.soop.net.socket.SoopChatSocket;
import kr.rtustudio.donation.service.soop.net.socket.SoopChatSocketHandler;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.*;

@Slf4j(topic = "DonationAPI/SOOP")
public class SoopLiveMonitor {

    private static final String CHATINFO_URL = "https://openapi.sooplive.com/broad/access/chatinfo";
    private static final OkHttpClient HTTP_CLIENT = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .callTimeout(15, TimeUnit.SECONDS)
            .build();

    private final SoopService service;
    private final SoopSubscriber subscriber;
    private final Map<String, SoopChatSocket> chatSockets = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> pollFuture;

    SoopLiveMonitor(@NotNull SoopService service, @NotNull SoopSubscriber subscriber) {
        this.service = service;
        this.subscriber = subscriber;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SOOP-LiveMonitor");
            t.setDaemon(true);
            return t;
        });
    }

    void start(long intervalSeconds) {
        pollFuture = scheduler.scheduleAtFixedRate(this::poll, 5, intervalSeconds, TimeUnit.SECONDS);
    }

    void checkImmediately(@NotNull String bjId) {
        scheduler.submit(() -> pollBjId(bjId));
    }

    private void poll() {
        Map<String, SoopToken> tokens = subscriber.getRegisteredTokens();
        if (tokens.isEmpty()) return;
        for (String bjId : tokens.keySet()) {
            pollBjId(bjId);
        }
    }

    private void pollBjId(@NotNull String bjId) {
        SoopChatSocket existing = chatSockets.get(bjId);
        if (existing != null && existing.isConnected()) return;

        SoopToken token = subscriber.getRegisteredTokens().get(bjId);
        if (token == null) return;

        try {
            ChatInfoResponse chatInfo = fetchChatInfo(token.accessToken());
            if (chatInfo == null) return;
            connectSocket(bjId, chatInfo);
        } catch (Throwable e) {
            log.warn("[LiveCheck] Poll failed for {}: {}", bjId, e.getMessage());
        }
    }

    private @Nullable ChatInfoResponse fetchChatInfo(@NotNull String accessToken) {
        RequestBody body = new FormBody.Builder()
                .add("access_token", accessToken)
                .build();
        Request request = new Request.Builder()
                .url(CHATINFO_URL)
                .post(body)
                .build();

        try (Response response = HTTP_CLIENT.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return null;

            JsonObject json = JsonParser.parseString(response.body().string()).getAsJsonObject();
            int result = json.has("result") ? json.get("result").getAsInt() : -1;
            if (result <= 0) {
                log.warn("[LiveCheck] chatinfo failed: result={}", result);
                return null;
            }

            JsonArray data = json.has("data") ? json.getAsJsonArray("data") : null;
            if (data == null || data.isEmpty()) return null;

            JsonObject entry = data.get(0).getAsJsonObject();
            String chatIp = entry.has("chat_ip") ? entry.get("chat_ip").getAsString() : null;
            int chatPort = entry.has("chat_port") ? entry.get("chat_port").getAsInt() : 0;
            int chatNo = entry.has("chat_no") ? entry.get("chat_no").getAsInt() : 0;
            String ticket = entry.has("key") ? entry.get("key").getAsString() : null;
            String bjId = entry.has("id") ? entry.get("id").getAsString() : "";
            String nick = entry.has("nick") ? entry.get("nick").getAsString() : null;
            int broadNo = entry.has("broad_no") ? entry.get("broad_no").getAsInt() : 0;
            String title = entry.has("title") ? entry.get("title").getAsString() : null;
            String cateName = entry.has("cate_name") ? entry.get("cate_name").getAsString() : null;
            String broadType = entry.has("broad_type") ? entry.get("broad_type").getAsString() : null;

            if (chatIp == null || ticket == null) return null;

            return new ChatInfoResponse(chatIp, chatPort, bjId, nick, broadNo, chatNo, ticket, title, cateName, broadType);
        } catch (Exception e) {
            log.warn("[LiveCheck] chatinfo exception: {}", e.getMessage());
            return null;
        }
    }

    private void connectSocket(@NotNull String bjId, @NotNull ChatInfoResponse chatInfo) {
        SoopChatSocket existing = chatSockets.get(bjId);
        if (existing != null) {
            existing.disconnect();
            chatSockets.remove(bjId);
        }

        SoopToken token = subscriber.getRegisteredTokens().get(bjId);

        SoopChatSocket socket = new SoopChatSocket(chatInfo, service.getConfig().getSocket(), new SoopChatSocketHandler() {
            @Override
            public void onConnected(@NotNull SoopChatSocket socket) {}

            @Override
            public void onJoined(@NotNull SoopChatSocket socket) {
                subscriber.onSocketJoined(bjId, token);
            }

            @Override
            public void onDonation(@NotNull SoopChatSocket socket, @NotNull String action, @NotNull SoopDonationMessage message) {
                subscriber.onDonationMessage(bjId, message);
            }

            @Override
            public void onDisconnected(@NotNull SoopChatSocket socket) {
                chatSockets.remove(bjId);
                subscriber.onSocketDisconnected(bjId);
            }

            @Override
            public void onError(@NotNull SoopChatSocket socket, @NotNull Throwable error) {
                log.warn("[Socket] Error for bjId={}: {}", bjId, error.getMessage());
                chatSockets.remove(bjId);
            }
        });

        chatSockets.put(bjId, socket);
        socket.connect();
    }

    void disconnectSocket(@NotNull String bjId) {
        SoopChatSocket socket = chatSockets.remove(bjId);
        if (socket != null) socket.disconnect();
    }

    boolean hasActiveSocket(@NotNull String bjId) {
        SoopChatSocket socket = chatSockets.get(bjId);
        return socket != null && socket.isConnected();
    }

    void closeAll() {
        if (pollFuture != null) {
            pollFuture.cancel(false);
            pollFuture = null;
        }
        chatSockets.values().forEach(SoopChatSocket::disconnect);
        chatSockets.clear();
        scheduler.shutdownNow();
    }
}
