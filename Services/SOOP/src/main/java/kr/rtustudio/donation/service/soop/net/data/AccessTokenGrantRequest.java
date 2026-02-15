package kr.rtustudio.donation.service.soop.net.data;

import org.jetbrains.annotations.NotNull;

public record AccessTokenGrantRequest(
        @NotNull String clientId,
        @NotNull String clientSecret,
        @NotNull String code
) {

    public @NotNull String grantType() {
        return "authorization_code";
    }

}
