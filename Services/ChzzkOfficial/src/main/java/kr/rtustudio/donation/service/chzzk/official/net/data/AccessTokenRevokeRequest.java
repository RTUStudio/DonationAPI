package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record AccessTokenRevokeRequest(
        @NotNull String clientId,
        @NotNull String clientSecret,
        @NotNull String token,
        @NotNull TokenTypeHint tokenTypeHint
) {

    public enum TokenTypeHint {
        ACCESS_TOKEN, REFRESH_TOKEN;

        public @NotNull String getAsString() {
            return this.toString().toLowerCase();
        }

    }

}
