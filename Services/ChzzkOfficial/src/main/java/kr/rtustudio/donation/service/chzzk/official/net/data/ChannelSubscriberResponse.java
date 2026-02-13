package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

public record ChannelSubscriberResponse(
        @NotNull List<Data> data
) {

    public record Data(
            @NotNull String channelId,
            @NotNull String channelName,
            @Range(from = 0, to = Integer.MAX_VALUE) int month,
            @Range(from = 1, to = 2) int tierNo,
            @NotNull String createdDate
    ) {
    }

}
