package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record ChatEventSubscribeRequest(
        @NotNull String sessionKey,
        @NotNull String accessToken
) {
}
