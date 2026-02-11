package kr.rtustudio.donation.service;

import kr.rtustudio.donation.common.Platform;

import java.util.List;

public interface Service {

    Services getType();

    default List<Platform> getPlatforms() {
        return getType().getPlatforms();
    }

    void start();

    void close();

}
