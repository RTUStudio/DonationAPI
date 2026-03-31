package kr.rtustudio.donation.service;

import kr.rtustudio.donation.common.Platform;
import lombok.Getter;

import java.util.List;

public enum Services {

    SSAPI("SSAPI", Platform.CHZZK, Platform.SOOP),
    CHZZK("CHZZK", Platform.CHZZK),
    SOOP("SOOP", Platform.SOOP),
    Youtube("Youtube", Platform.YOUTUBE),
    Toonation("Toonation", Platform.TOONATION),
    CIME("CIME", Platform.CIME);

    @Getter
    private final String storage;

    @Getter
    private final List<Platform> platforms;

    Services(String storage, Platform... platforms) {
        this.storage = storage;
        this.platforms = List.of(platforms);
    }

}
