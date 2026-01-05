package kr.rtustudio.donation.service;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.Platform;

import java.util.List;
import java.util.function.Consumer;

public interface Service {

    Services getType();

    default List<Platform> getPlatforms() {
        return getType().getPlatforms();
    }

    void start(Consumer<Donation> donationHandler);

    void close();

}
