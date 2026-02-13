package kr.rtustudio.donation.service.chzzk.official.data;

import kr.rtustudio.donation.service.chzzk.official.net.data.ChannelManagerResponse;
import kr.rtustudio.donation.service.chzzk.official.utils.Utils;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public record ChzzkChannelManager(
        @NotNull String id,
        @NotNull String name,
        @NotNull ChzzkChannelManagerRole role,
        @NotNull ZonedDateTime appointDate
) {

    public static @NotNull ChzzkChannelManager of(@NotNull ChannelManagerResponse.Data response) {
        return new ChzzkChannelManager(
                response.managerChannelId(),
                response.managerChannelName(),
                ChzzkChannelManagerRole.valueOf(response.userRole()),
                Utils.parseZonedDateTime(response.createdDate())
        );
    }

}
