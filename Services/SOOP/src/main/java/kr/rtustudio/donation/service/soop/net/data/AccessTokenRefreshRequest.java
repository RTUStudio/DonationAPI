package kr.rtustudio.donation.service.soop.net.data;

import org.jetbrains.annotations.NotNull;

public record AccessTokenRefreshRequest(
        @NotNull String clientId,
        @NotNull String clientSecret,
        @NotNull String refreshToken
) {

    public @NotNull String grantType() {
        return "refresh_token";
    }

}
