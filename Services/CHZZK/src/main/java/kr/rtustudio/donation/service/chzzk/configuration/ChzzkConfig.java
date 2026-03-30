package kr.rtustudio.donation.service.chzzk.configuration;

import kr.rtustudio.donation.common.configuration.SocketServiceConfig;

public interface ChzzkConfig extends SocketServiceConfig {

    String getClientId();

    String getClientSecret();

    String getBaseUri();

    String getHost();

    int getPort();
}
