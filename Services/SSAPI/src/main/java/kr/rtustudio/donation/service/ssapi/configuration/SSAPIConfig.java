package kr.rtustudio.donation.service.ssapi.configuration;

import kr.rtustudio.donation.common.configuration.ServiceConfig;

public interface SSAPIConfig extends ServiceConfig {

    String getApiKey();

    int getLoginRetryDelay();
}
