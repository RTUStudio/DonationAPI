package kr.rtustudio.donation.service.ssapi.configuration;

import kr.rtustudio.donation.common.configuration.SocketServiceConfig;

public interface SSAPIConfig extends SocketServiceConfig {

    String getApiKey();

    int getLoginRetryDelay();
}
