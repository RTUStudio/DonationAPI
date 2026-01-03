package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;

public record DonationEventSubscribeRequest(
        @NotNull String sessionKey,
        @NotNull String accessToken
) {
}
