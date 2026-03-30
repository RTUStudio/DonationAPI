package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ChannelFollowerResponse(
        @NotNull List<Data> data
) {

    public record Data(
            @NotNull String channelId,
            @NotNull String channelName,
            @NotNull String createdDate
    ) {
    }

}
