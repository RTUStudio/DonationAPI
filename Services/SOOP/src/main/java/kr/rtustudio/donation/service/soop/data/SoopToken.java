package kr.rtustudio.donation.service.soop.data;

import org.jetbrains.annotations.NotNull;

public record SoopToken(@NotNull String accessToken, @NotNull String refreshToken) {
}
