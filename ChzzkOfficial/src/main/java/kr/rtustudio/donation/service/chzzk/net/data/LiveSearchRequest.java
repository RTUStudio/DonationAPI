package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public record LiveSearchRequest(
        @Range(from = 1, to = 20) int size,
        @NotNull String next,
        @NotNull String accessToken
) {
}
