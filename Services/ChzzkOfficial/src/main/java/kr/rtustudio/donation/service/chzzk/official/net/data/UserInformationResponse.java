package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record UserInformationResponse(
        @NotNull String channelId,
        @NotNull String channelName
) {
}
