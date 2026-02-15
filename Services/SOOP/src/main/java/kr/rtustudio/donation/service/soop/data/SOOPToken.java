package kr.rtustudio.donation.service.soop.data;

import org.jetbrains.annotations.NotNull;

public record SOOPToken(@NotNull String accessToken, @NotNull String refreshToken) {
}
