package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;

public record LiveSettingsRequest(
        @NotNull String accessToken
) {
}
