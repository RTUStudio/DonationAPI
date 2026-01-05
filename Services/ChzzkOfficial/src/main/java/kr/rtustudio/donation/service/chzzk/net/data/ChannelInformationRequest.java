package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ChannelInformationRequest(
        @NotNull String clientId,
        @NotNull String clientSecret,
        @NotNull List<String> channelIds
) {
}
