package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public record ChannelSubscriberRequest(
        @Range(from = 0, to = Integer.MAX_VALUE) int page,
        @Range(from = 1, to = 50) int size,
        @NotNull String sort,
        @NotNull String accessToken
) {
}
