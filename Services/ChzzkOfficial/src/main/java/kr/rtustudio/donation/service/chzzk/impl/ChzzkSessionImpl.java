package kr.rtustudio.donation.service.chzzk.impl;

import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.ChzzkSession;
import kr.rtustudio.donation.service.chzzk.data.ChzzkChatMessage;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationMessage;
import kr.rtustudio.donation.service.chzzk.data.ChzzkSessionUrl;
import kr.rtustudio.donation.service.chzzk.net.data.message.ChatMessage;
import kr.rtustudio.donation.service.chzzk.net.data.message.ConnectedMessage;
import kr.rtustudio.donation.service.chzzk.net.data.message.DonationMessage;
import kr.rtustudio.donation.service.chzzk.net.socket.SessionSocket;
import kr.rtustudio.donation.service.chzzk.net.socket.SessionSocketHandler;
import kr.rtustudio.donation.service.chzzk.net.socket.impl.SessionSocketImpl;
import kr.rtustudio.donation.service.chzzk.utils.Logger;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Getter
public class ChzzkSessionImpl implements ChzzkSession {

    private final @NotNull Chzzk chzzk;

    private SessionSocket socket;

    ChzzkSessionImpl(@NotNull Chzzk chzzk) {
        this.chzzk = chzzk;
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
            Optional<ChzzkSessionUrl> opt = chzzk.getSessionUrl().join();
            if (opt.isEmpty()) {
                Logger.error("No session url found. disconnecting.");
                disconnect();
                return;
            }

            ChzzkSessionUrl url = opt.get();
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
                    chzzk.subscribeDonation(sessionKey);
                }

                @Override
                public void onChatMessageReceived(@NotNull SessionSocket session, @NotNull ChatMessage message) {
                    ChzzkChatMessage chzzkMessage = ChzzkChatMessage.of(message);
                    chzzk.getHandler().onChatMessage(chzzk, chzzkMessage);
                }

                @Override
                public void onDonationMessageReceived(@NotNull SessionSocket session, @NotNull DonationMessage message) {
                    ChzzkDonationMessage chzzkMessage = ChzzkDonationMessage.of(message);
                    chzzk.getHandler().onDonationMessage(chzzk, chzzkMessage);
                }

            });
            socket.connect();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
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
