package kr.rtustudio.donation.service.soop.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record SOOPDonationMessage(
        @NotNull SOOPDonationType donationType,
        @Nullable String bjId,
        @Nullable String userId,
        @Nullable String userNickname,
        int count,
        @Nullable String message
) {
}
