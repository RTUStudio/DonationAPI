package kr.rtustudio.donation.service.chzzk.official.data;

import kr.rtustudio.donation.service.chzzk.official.net.data.ChannelFollowerResponse;
import kr.rtustudio.donation.service.chzzk.official.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public record ChzzkChannelFollower(
        @NotNull String id,
        @NotNull String name,
        @NotNull ZonedDateTime followDate
) {

    public static @NotNull ChzzkChannelFollower of(@NotNull ChannelFollowerResponse.Data response) {
        return new ChzzkChannelFollower(
                response.channelId(),
                response.channelName(),
                Utils.parseZonedDateTime(response.createdDate())
        );
    }

}
