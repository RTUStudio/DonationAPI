package kr.rtustudio.donation.service.chzzk.official.data;

import org.jetbrains.annotations.NotNull;

public record ChzzkToken(
        @NotNull String accessToken,
        @NotNull String refreshToken
) {
}
