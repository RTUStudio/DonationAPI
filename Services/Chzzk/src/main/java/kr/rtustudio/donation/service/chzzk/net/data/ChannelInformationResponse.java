package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.List;

public record ChannelInformationResponse(
        @NotNull List<Data> data
) {

    public record Data(
            @NotNull String channelId,
            @NotNull String channelName,
            @NotNull String channelImageUrl,
            @Range(from = 0, to = Integer.MAX_VALUE) int followerCount,
            boolean verifiedMark
    ) {
    }

}
