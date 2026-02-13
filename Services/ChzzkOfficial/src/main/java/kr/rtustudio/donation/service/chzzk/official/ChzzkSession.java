package kr.rtustudio.donation.service.chzzk.official;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface ChzzkSession {

    @NotNull Chzzk getChzzk();

    @NotNull Optional<String> getUrl();

    @NotNull Optional<String> getSessionKey();

    @NotNull CompletableFuture<Void> connectAsync();

    void connect();

    @NotNull CompletableFuture<Void> disconnectAsync();

    void disconnect();

    boolean isConnected();

}
