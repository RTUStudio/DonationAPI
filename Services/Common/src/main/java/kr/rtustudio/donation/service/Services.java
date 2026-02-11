package kr.rtustudio.donation.service;

import kr.rtustudio.donation.common.Platform;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public enum Services {

    SSAPI("SSAPI", Platform.CHZZK, Platform.SOOP),
    ChzzkOfficial("ChzzkOfficial", Platform.CHZZK),
    ChzzkUnofficial("ChzzkUnofficial", Platform.CHZZK),
    SOOP("SOOP", Platform.SOOP),
    Youtube("Youtube", Platform.YOUTUBE),
    Toonation("Toonation", Platform.TOONATION);

    @Getter
    private final String storage;

    @Getter
    private final List<Platform> platforms;

    Services(String storage, Platform... platforms) {
        this.storage = storage;
        this.platforms = List.of(platforms);
    }

}
