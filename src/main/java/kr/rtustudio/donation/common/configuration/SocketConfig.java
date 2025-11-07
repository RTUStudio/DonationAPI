package kr.rtustudio.donation.common.configuration;

public interface SocketConfig {

    int getTimeout();

    int getLoginRetryDelay();

    boolean isKeepaliveEnabled();

    int getKeepaliveInterval();

    boolean isReconnectionEnabled();

    int getReconnectionDelay();

    int getReconnectionMaxDelay();
}
