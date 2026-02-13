package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record AuthorizationCodeRequest(
        @NotNull String clientId,
        @NotNull String redirectUri,
        @NotNull String state
) {
}
