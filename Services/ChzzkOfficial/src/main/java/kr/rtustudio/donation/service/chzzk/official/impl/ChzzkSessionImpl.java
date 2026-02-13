package kr.rtustudio.donation.service.chzzk.official.impl;

import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.chzzk.official.Chzzk;
import kr.rtustudio.donation.service.chzzk.official.ChzzkSession;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkChatMessage;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkDonationMessage;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkSessionUrl;
import kr.rtustudio.donation.service.chzzk.official.net.data.message.ChatMessage;
import kr.rtustudio.donation.service.chzzk.official.net.data.message.ConnectedMessage;
import kr.rtustudio.donation.service.chzzk.official.net.data.message.DonationMessage;
import kr.rtustudio.donation.service.chzzk.official.net.socket.SessionSocket;
import kr.rtustudio.donation.service.chzzk.official.net.socket.SessionSocketHandler;
import kr.rtustudio.donation.service.chzzk.official.net.socket.impl.SessionSocketImpl;
import kr.rtustudio.donation.service.chzzk.official.utils.Logger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.*;

@Getter
public class ChzzkSessionImpl implements ChzzkSession {

    private final @NotNull Chzzk chzzk;
    private final @Nullable SocketOption socketOption;

    private SessionSocket socket;
    private ScheduledExecutorService keepaliveExecutor;

    ChzzkSessionImpl(@NotNull Chzzk chzzk, @Nullable SocketOption socketOption) {
        this.chzzk = chzzk;
        this.socketOption = socketOption;
    }

    @Override
    public @NotNull Optional<String> getUrl() {
        if (socket == null) {
            return Optional.empty();
        }
        return Optional.of(socket.getUrl());
    }

    @Override
    public @NotNull Optional<String> getSessionKey() {
        if (socket == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(socket.getSessionKey());
    }

    @Override
    public @NotNull CompletableFuture<Void> connectAsync() {
        return CompletableFuture.runAsync(this::connect);
    }

    @Override
    public void connect() {
        if (socket != null) {
            throw new IllegalStateException("Session is already connected");
        }

        try {
            Logger.info("Requesting session URL...");
            Optional<ChzzkSessionUrl> opt = chzzk.getSessionUrl();
            if (opt.isEmpty()) {
                Logger.error("No session url found. disconnecting.");
                disconnect();
                return;
            }

            ChzzkSessionUrl url = opt.get();
            Logger.info("Session URL obtained: " + url.url());
            socket = new SessionSocketImpl(url.url(), new SessionSocketHandler() {

                @Override
                public void onInvalidResponse(@NotNull SessionSocket session, @NotNull Object[] response) {
                    Logger.error("Receive invalid response (" + Arrays.toString(response) + "). disconnecting.");
                    session.disconnect();
                }

                @Override
                public void onConnected(@NotNull SessionSocket session, @NotNull ConnectedMessage message) {
                    String sessionKey = session.getSessionKey();
                    if (sessionKey == null || sessionKey.isEmpty()) {
                        Logger.error("Session key must not be null or empty. disconnecting.");
                        session.disconnect();
                        return;
                    }
                    try {
                        chzzk.subscribeDonation(sessionKey);
                        Logger.info("Successfully subscribed donation for session: " + sessionKey);
                    } catch (Exception e) {
                        Logger.error("Failed to subscribe donation for session: " + sessionKey + " - " + e.getMessage());
                        session.disconnect();
                    }
                }

                @Override
                public void onChatMessageReceived(@NotNull SessionSocket session, @NotNull ChatMessage message) {
                    ChzzkChatMessage chzzkMessage = ChzzkChatMessage.of(message);
                    if (chzzk instanceof ChzzkImpl impl) {
                        impl.getHandlers().forEach(handler -> handler.onChatMessage(chzzk, chzzkMessage));
                    }
                }

                @Override
                public void onDonationMessageReceived(@NotNull SessionSocket session, @NotNull DonationMessage message) {
                    ChzzkDonationMessage chzzkMessage = ChzzkDonationMessage.of(message);
                    if (chzzk instanceof ChzzkImpl impl) {
                        impl.getHandlers().forEach(handler -> handler.onDonationMessage(chzzk, chzzkMessage));
                    }
                }

            }, socketOption);
            socket.connect();
            startKeepalive();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private void startKeepalive() {
        if (socketOption == null || !socketOption.isKeepaliveEnabled()) return;
        int interval = socketOption.getKeepaliveInterval();
        keepaliveExecutor = Executors.newSingleThreadScheduledExecutor();
        keepaliveExecutor.scheduleAtFixedRate(() -> {
            if (socket != null && socket.isConnected()) {
                socket.ping();
            }
        }, interval, interval, TimeUnit.MILLISECONDS);
        Logger.info("Keepalive started (interval: " + interval + "ms)");
    }

    private void stopKeepalive() {
        if (keepaliveExecutor != null) {
            keepaliveExecutor.shutdownNow();
            keepaliveExecutor = null;
        }
    }

    @Override
    public @NotNull CompletableFuture<Void> disconnectAsync() {
        return CompletableFuture.runAsync(this::disconnect);
    }

    @Override
    public void disconnect() {
        if (socket == null) {
            Logger.error("Session is not connected");
            return;
        }

        try {
            stopKeepalive();
            String sessionKey = socket.getSessionKey();
            if (sessionKey != null) {
                chzzk.unsubscribeChat(sessionKey);
                chzzk.unsubscribeDonation(sessionKey);
            }
        } finally {
            socket.disconnect();
            socket = null;
        }
    }

    @Override
    public boolean isConnected() {
        return socket != null && socket.isConnected();
    }

}
