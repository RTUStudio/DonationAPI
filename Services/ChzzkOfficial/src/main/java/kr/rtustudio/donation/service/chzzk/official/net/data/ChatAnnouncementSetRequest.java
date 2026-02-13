package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record ChatAnnouncementSetRequest(
        @NotNull String message,
        @NotNull String messageId,
        @NotNull String accessToken
) {
}
