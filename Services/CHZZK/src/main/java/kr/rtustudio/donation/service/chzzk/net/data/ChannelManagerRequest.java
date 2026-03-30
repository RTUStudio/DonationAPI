package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;

public record ChannelManagerRequest(
        @NotNull String accessToken
) {
}
