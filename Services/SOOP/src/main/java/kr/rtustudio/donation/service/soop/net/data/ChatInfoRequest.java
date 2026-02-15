package kr.rtustudio.donation.service.soop.net.data;

import org.jetbrains.annotations.NotNull;

public record ChatInfoRequest(
        @NotNull String accessToken
) {
}
