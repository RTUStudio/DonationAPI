package kr.rtustudio.donation.common.configuration;

public interface SocketOption {

    int getTimeout();

    boolean isKeepaliveEnabled();

    int getKeepaliveInterval();

    boolean isReconnectionEnabled();

    int getReconnectionDelay();

    int getReconnectionMaxDelay();
}
