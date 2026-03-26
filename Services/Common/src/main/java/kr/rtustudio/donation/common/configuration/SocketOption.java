package kr.rtustudio.donation.common.configuration;

public interface SocketOption {

    int getTimeout();

    KeepaliveOption getKeepalive();

    ReconnectionOption getReconnection();

    interface KeepaliveOption {
        boolean isEnabled();
        int getInterval();
    }

    interface ReconnectionOption {
        boolean isEnabled();
        int getDelay();
        int getMaxDelay();
    }
}
