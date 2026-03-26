package kr.rtustudio.donation.service.youtube.configuration;

import kr.rtustudio.donation.common.configuration.ServiceConfig;

public interface YoutubeConfig extends ServiceConfig {
    int getPollingIntervalMs();
}
