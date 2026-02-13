package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record AccessTokenRefreshResponse(
        @NotNull String accessToken,
        @NotNull String refreshToken,
        @NotNull String tokenType,
        @NotNull String expiresIn,
        @NotNull String scope
) {
}
