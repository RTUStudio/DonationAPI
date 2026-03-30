package kr.rtustudio.donation.service.soop.net.socket;

import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.soop.data.SoopDonationMessage;
import kr.rtustudio.donation.service.soop.net.data.ChatInfoResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.ByteString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    private @Nullable WebSocket webSocket;
    private @Nullable ScheduledFuture<?> keepAliveFuture;
    private final ScheduledExecutorService scheduler;

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
                .build();

        log.info("Started new SOOP WebSocket for bjId: {}", bjId);
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

    @Override
    public void onOpen(@NotNull WebSocket ws, @NotNull Response response) {
        log.info("SOOP socket connected for bjId: {}", bjId);
        // SDK: e.push(l), e.push(ticket), e.push(l), e.push(flag1), e.push(l)
        ws.send(SoopPacket.encode(SoopServiceCode.SVC_SDK_LOGIN, "", ticket, "", "", ""));
    }

    @Override
    public void onMessage(@NotNull WebSocket ws, @NotNull ByteString bytes) {
        SoopPacket.ParsedPacket packet = SoopPacket.parse(bytes.toByteArray());
        if (packet == null || packet.retCode() < 0) return;

        if (packet.serviceCode() == SoopServiceCode.SVC_CLOSE_BROAD) {
            log.info("Broadcast closed for bjId: {}, disconnecting", bjId);
            disconnect();
            return;
        }

        SoopMessageParser.ParsedMessage parsed = SoopMessageParser.parse(packet.serviceCode(), packet.fields());
        if (parsed == null) return;

        switch (parsed.action()) {
            case "LOGIN" -> {
                log.info("Login OK for bjId: {}, joining room: {}", bjId, chatNo);
                // SDK: e.push(l), e.push(roomId), e.push(l), e.push(ticket), e.push(l), e.push(5), e.push(l), e.push(""), e.push(l), e.push(logInfo), e.push(l)
                ws.send(SoopPacket.encode(SoopServiceCode.SVC_JOINCH,
                        "", String.valueOf(chatNo), "", ticket, "", "5", "", "", "", ""));
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
            case "JOIN" -> {
                log.info("Joined room for bjId: {}", bjId);
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

    @Override
    public void onClosing(@NotNull WebSocket ws, int code, @NotNull String reason) {
        log.info("SOOP socket closing for bjId: {}: {} {}", bjId, code, reason);
    }

    @Override
    public void onClosed(@NotNull WebSocket ws, int code, @NotNull String reason) {
        log.info("SOOP socket closed for bjId: {}: {} {}", bjId, code, reason);
        stopKeepAlive();
        handler.onDisconnected(this);
    }

    @Override
    public void onFailure(@NotNull WebSocket ws, @NotNull Throwable t, @Nullable Response response) {
        log.error("SOOP socket failure for bjId: {}: {}", bjId, t.getMessage());
        stopKeepAlive();
        handler.onError(this, t);
    }

}
