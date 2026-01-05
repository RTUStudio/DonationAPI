package kr.rtustudio.donation.service.chzzk.net.data;

import org.jetbrains.annotations.NotNull;

public record ChatSettingsRequest(
        @NotNull String accessToken
) {
}
