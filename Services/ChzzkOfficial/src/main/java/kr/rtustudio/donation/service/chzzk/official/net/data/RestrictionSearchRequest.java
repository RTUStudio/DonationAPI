package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public record RestrictionSearchRequest(
        @Range(from = 0, to = Integer.MAX_VALUE) int size,
        @NotNull String next,
        @NotNull String accessToken
) {
}
