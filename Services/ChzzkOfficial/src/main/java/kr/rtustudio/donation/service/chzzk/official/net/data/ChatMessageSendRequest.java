package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record ChatMessageSendRequest(
        @NotNull String message,
        @NotNull String accessToken
) {
}
