package kr.rtustudio.donation.common.configuration;

public interface ServiceConfig {

    boolean isEnabled();

    Socket getSocket();

    interface Socket {

        int getTimeout();

        boolean isKeepaliveEnabled();

        int getKeepaliveInterval();

        boolean isReconnectionEnabled();

        int getReconnectionDelay();

        int getReconnectionMaxDelay();
    }
}
