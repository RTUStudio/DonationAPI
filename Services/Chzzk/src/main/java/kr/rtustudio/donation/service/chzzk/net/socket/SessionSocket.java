package kr.rtustudio.donation.service.chzzk.net.socket;

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
