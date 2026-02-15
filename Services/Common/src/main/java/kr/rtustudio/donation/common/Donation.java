package kr.rtustudio.donation.common;

import kr.rtustudio.donation.service.Services;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public record Donation(
        @Nullable UUID uniqueId,
        @NotNull Services service,
        @NotNull Platform platform,
        @NotNull DonationType type,
        @NotNull String streamer,
        @NotNull String donator,
        @NotNull String nickname,
        @NotNull String message,
        int amount
) {
    public Donation {
        type = type != null ? type : DonationType.CHAT;
        streamer = streamer != null ? streamer : "";
        donator = donator != null ? donator : "";
        nickname = nickname != null ? nickname : "익명의 후원자";
        message = message != null ? message : "";
    }
}
