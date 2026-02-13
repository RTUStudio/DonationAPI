package kr.rtustudio.donation.service.chzzk.official.data;

import kr.rtustudio.donation.service.chzzk.official.net.data.RestrictionSearchResponse;
import kr.rtustudio.donation.service.chzzk.official.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public record ChzzkRestrictionChannel(
        @NotNull String id,
        @NotNull String name,
        @NotNull ZonedDateTime restrictDate,
        @NotNull ZonedDateTime releaseDate
) {

    public static @NotNull ChzzkRestrictionChannel of(@NotNull RestrictionSearchResponse response) {
        return new ChzzkRestrictionChannel(
                response.restrictedChannelId(),
                response.restrictedChannelName(),
                Utils.parseZonedDateTime(response.createdDate()),
                Utils.parseZonedDateTime(response.releaseDate())
        );
    }

}
