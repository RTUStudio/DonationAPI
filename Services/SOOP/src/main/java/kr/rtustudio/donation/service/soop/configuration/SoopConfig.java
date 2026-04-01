package kr.rtustudio.donation.service.soop.configuration;

import kr.rtustudio.donation.common.configuration.SocketServiceConfig;

public interface SoopConfig extends SocketServiceConfig {

    String getClientId();

    String getClientSecret();

    String getBaseUri();

    String getHost();

    int getPort();

    long getLiveCheckInterval();
}
