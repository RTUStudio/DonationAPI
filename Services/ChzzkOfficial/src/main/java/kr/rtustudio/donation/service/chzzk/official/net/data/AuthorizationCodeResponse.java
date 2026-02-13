package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record AuthorizationCodeResponse(
        @NotNull String code,
        @NotNull String state
) {
}
