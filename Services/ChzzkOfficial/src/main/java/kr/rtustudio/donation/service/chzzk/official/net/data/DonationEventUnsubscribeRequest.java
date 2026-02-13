package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record DonationEventUnsubscribeRequest(
        @NotNull String sessionKey,
        @NotNull String accessToken
) {
}
