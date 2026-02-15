package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;

public record SessionUrlRequest(
        @NotNull String accessToken
) {
}
