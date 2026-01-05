package kr.rtustudio.donation.common.configuration;

public interface ServiceConfig {

    boolean isEnabled();

    Socket getSocket();

    public interface Socket {

        int getTimeout();

        int getLoginRetryDelay();

        boolean isKeepaliveEnabled();

        int getKeepaliveInterval();

        boolean isReconnectionEnabled();

        int getReconnectionDelay();

        int getReconnectionMaxDelay();
    }
}
