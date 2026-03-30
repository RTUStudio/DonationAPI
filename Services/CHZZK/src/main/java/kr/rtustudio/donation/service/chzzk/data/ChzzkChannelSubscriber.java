package kr.rtustudio.donation.service.chzzk.data;

import kr.rtustudio.donation.service.chzzk.net.data.ChannelSubscriberResponse;
import kr.rtustudio.donation.service.chzzk.utils.Utils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.ZonedDateTime;

public record ChzzkChannelSubscriber(
        @NotNull String id,
        @NotNull String name,
        @Range(from = 0, to = Integer.MAX_VALUE) int month,
        @NotNull ChzzkChannelSubscriberTier tier,
        @NotNull ZonedDateTime subscribeDate
) {

    public static @NotNull ChzzkChannelSubscriber of(@NotNull ChannelSubscriberResponse.Data response) {
        return new ChzzkChannelSubscriber(
                response.channelId(),
                response.channelName(),
                response.month(),
                ChzzkChannelSubscriberTier.from(response.tierNo()),
                Utils.parseZonedDateTime(response.createdDate())
        );
    }

}
