package kr.rtustudio.donation.service;

import kr.rtustudio.donation.common.Platform;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@RequiredArgsConstructor
public enum Services {

    SSAPI(Platform.CHZZK, Platform.SOOP),
    CHZZK_OFFICIAL(Platform.CHZZK);

    @Getter
    private final List<Platform> platforms;

    Services(Platform... platforms) {
        this.platforms = List.of(platforms);
    }

}
