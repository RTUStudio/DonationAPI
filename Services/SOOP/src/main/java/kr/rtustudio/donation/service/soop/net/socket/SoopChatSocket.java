package kr.rtustudio.donation.service.soop.net.socket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.soop.data.SoopDonationMessage;
import kr.rtustudio.donation.service.soop.net.data.ChatInfoResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "DonationAPI/SOOP")
public class SoopChatSocket extends WebSocketListener {

    private static final OkHttpClient SHARED_HTTP_CLIENT;

    static {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(1024);
        dispatcher.setMaxRequestsPerHost(1024);
        SHARED_HTTP_CLIENT = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
    }

    @Getter
    private final @NotNull String bjId;
    private final @NotNull String ticket;
    private final int chatNo;
    private final @NotNull String wsUrl;
    private final @NotNull SocketOption socketOption;
    private final @NotNull SoopChatSocketHandler handler;
    private final ScheduledExecutorService scheduler;
    private @Nullable WebSocket webSocket;
    private @Nullable ScheduledFuture<?> keepAliveFuture;

    @Getter
    private volatile boolean connected = false;
    @Getter
    private volatile boolean joined = false;

    public SoopChatSocket(@NotNull ChatInfoResponse chatInfo, @NotNull SocketOption socketOption, @NotNull SoopChatSocketHandler handler) {
        this.bjId = chatInfo.bjId();
        this.ticket = chatInfo.ticket();
        this.chatNo = chatInfo.chatNo();
        this.wsUrl = buildWebSocketUrl(chatInfo.chatIp(), chatInfo.chatPort(), chatInfo.bjId());
        this.socketOption = socketOption;
        this.handler = handler;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "SOOP-KeepAlive-" + bjId);
            t.setDaemon(true);
            return t;
        });
    }

    private static @NotNull String buildWebSocketUrl(@NotNull String ip, int port, @NotNull String bjId) {
        String[] parts = ip.split("\\.");
        StringBuilder hex = new StringBuilder();
        for (String part : parts) {
            hex.append(String.format("%02X", Integer.parseInt(part)));
        }
        return "wss://chat-" + hex + ".sooplive.com:" + (port + 1) + "/Websocket/" + bjId;
    }

    public void connect() {
        Request request = new Request.Builder()
                .url(wsUrl)
                .header("Sec-WebSocket-Protocol", "chat")
                .header("Origin", "https://play.sooplive.com")
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0 Safari/537.36")
                .build();

        OkHttpClient client = SHARED_HTTP_CLIENT.newBuilder()
                .readTimeout(socketOption.getTimeout(), TimeUnit.MILLISECONDS)
                .build();
        webSocket = client.newWebSocket(request, this);
    }

    public void disconnect() {
        stopKeepAlive();
        if (webSocket != null) {
            webSocket.close(1000, "Disconnecting");
            webSocket = null;
        }
        scheduler.shutdownNow();
    }

    private void stopKeepAlive() {
        connected = false;
        if (keepAliveFuture != null) {
            keepAliveFuture.cancel(false);
            keepAliveFuture = null;
        }
    }

    // ── WebSocket Callbacks ──

    @Override
    public void onOpen(@NotNull WebSocket ws, @NotNull Response response) {
        // SVC_LOGIN: 비로그인(게스트) 연결 — SDK 필드 구조: [sep, sep, sep, "16", sep]
        ws.send(SoopPacket.encode(SoopServiceCode.SVC_LOGIN, "", "", "16", ""));
    }

    @Override
    public void onMessage(@NotNull WebSocket ws, @NotNull String text) {
        handleRawMessage(ws, text.getBytes(StandardCharsets.ISO_8859_1));
    }

    @Override
    public void onMessage(@NotNull WebSocket ws, @NotNull ByteString bytes) {
        handleRawMessage(ws, bytes.toByteArray());
    }

    private void handleRawMessage(@NotNull WebSocket ws, byte[] raw) {
        SoopPacket.ParsedPacket packet = SoopPacket.parse(raw);
        if (packet == null) return;
        if (packet.retCode() < 0) return;

        if (packet.serviceCode() == SoopServiceCode.SVC_CLOSE_BROAD) {
            disconnect();
            return;
        }

        SoopMessageParser.ParsedMessage parsed = SoopMessageParser.parse(packet.serviceCode(), packet.fields());
        if (parsed == null) return;

        switch (parsed.action()) {
            case "LOGIN" -> handleLogin(ws, packet);
            case "JOIN" -> {
                joined = true;
                handler.onJoined(this);
            }
            default -> {
                if (parsed.message() instanceof SoopDonationMessage msg) {
                    handler.onDonation(this, parsed.action(), msg);
                }
            }
        }
    }

    private void handleLogin(@NotNull WebSocket ws, @NotNull SoopPacket.ParsedPacket packet) {
        // LOGIN 응답에 JSON 오류가 포함되었는지 검증
        for (String field : packet.fields()) {
            if (!field.startsWith("{")) continue;
            try {
                JsonObject json = JsonParser.parseString(field).getAsJsonObject();
                if (json.has("error")) {
                    log.error("[Socket] Login failed: bjId={}, error={}", bjId, json.get("error").getAsString());
                    disconnect();
                    return;
                }
                if (json.has("result") && json.get("result").getAsInt() < 0) {
                    log.error("[Socket] Login failed: bjId={}, result={}", bjId, json.get("result").getAsInt());
                    disconnect();
                    return;
                }
            } catch (Exception ignored) {}
        }

        // JOINCH 전송
        ws.send(SoopPacket.encode(SoopServiceCode.SVC_JOINCH,
                String.valueOf(chatNo), ticket, "5", "", "", ""));

        // Keepalive 시작
        if (socketOption.getKeepalive().isEnabled()) {
            long interval = socketOption.getKeepalive().getInterval();
            keepAliveFuture = scheduler.scheduleAtFixedRate(() -> {
                if (webSocket != null && connected) {
                    webSocket.send(SoopPacket.encode(SoopServiceCode.SVC_KEEPALIVE, ""));
                }
            }, interval, interval, TimeUnit.MILLISECONDS);
        }
        connected = true;
        handler.onConnected(this);
    }

    @Override
    public void onClosing(@NotNull WebSocket ws, int code, @NotNull String reason) {}

    @Override
    public void onClosed(@NotNull WebSocket ws, int code, @NotNull String reason) {
        stopKeepAlive();
        handler.onDisconnected(this);
    }

    @Override
    public void onFailure(@NotNull WebSocket ws, @NotNull Throwable t, @Nullable Response response) {
        log.warn("[Socket] Connection failure: bjId={}, error={}", bjId, t.getMessage());
        stopKeepAlive();
        handler.onError(this, t);
    }
}
