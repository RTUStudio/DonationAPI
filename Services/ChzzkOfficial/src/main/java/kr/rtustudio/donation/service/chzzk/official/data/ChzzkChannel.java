package kr.rtustudio.donation.service.chzzk.official.data;

import kr.rtustudio.donation.service.chzzk.official.net.data.ChannelInformationResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public record ChzzkChannel(
        @NotNull String id,
        @NotNull String name,
        @NotNull String imageUrl,
        @Range(from = 0, to = Integer.MAX_VALUE) int followerCount,
        boolean verifiedMark
) {

    public static @NotNull ChzzkChannel of(@NotNull ChannelInformationResponse.Data response) {
        return of(response.channelId(), response.channelName(), response.channelImageUrl(), response.followerCount(), response.verifiedMark());
    }

    public static @NotNull ChzzkChannel of(
            @NotNull String id, @NotNull String name, @NotNull String imageUrl,
            @Range(from = 0, to = Integer.MAX_VALUE) int followerCount,
            boolean verifiedMark
    ) {
        return new ChzzkChannel(id, name, imageUrl, followerCount, verifiedMark);
    }

}
