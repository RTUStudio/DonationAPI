package kr.rtustudio.donation.service.chzzk.official.data;

import kr.rtustudio.donation.service.chzzk.official.net.data.LiveStreamKeyResponse;
import org.jetbrains.annotations.NotNull;

public record ChzzkLiveStreamKey(
        @NotNull String streamKey
) {

    public static @NotNull ChzzkLiveStreamKey of(@NotNull LiveStreamKeyResponse response) {
        return of(response.streamKey());
    }

    public static @NotNull ChzzkLiveStreamKey of(@NotNull String key) {
        return new ChzzkLiveStreamKey(key);
    }

}
