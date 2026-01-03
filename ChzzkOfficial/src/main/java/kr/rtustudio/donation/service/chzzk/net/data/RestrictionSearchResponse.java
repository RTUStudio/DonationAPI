package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;

public record RestrictionSearchResponse(
        @NotNull String restrictedChannelId,
        @NotNull String restrictedChannelName,
        @NotNull String createdDate,
        @NotNull String releaseDate
) {
}
