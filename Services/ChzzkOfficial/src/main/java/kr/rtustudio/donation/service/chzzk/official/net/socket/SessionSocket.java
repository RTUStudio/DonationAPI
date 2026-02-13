package kr.rtustudio.donation.service.chzzk.official.net.socket;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SessionSocket {

    @NotNull String getUrl();

    @Nullable String getSessionKey();

    void connect();

    void disconnect();

    void ping();

    boolean isConnected();

}
