package kr.rtustudio.donation.service.chzzk.official.configuration;

import kr.rtustudio.donation.common.configuration.ServiceConfig;

public interface ChzzkConfig extends ServiceConfig {

    String getClientId();

    String getClientSecret();

    String getBaseUri();

    String getHost();

    int getPort();
}
