package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;

public record RestrictionAddRequest(
        @NotNull String targetChannelId,
        @NotNull String accessToken
) {
}
