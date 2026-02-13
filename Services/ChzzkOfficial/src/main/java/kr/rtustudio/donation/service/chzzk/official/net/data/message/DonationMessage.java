package kr.rtustudio.donation.service.chzzk.official.net.data.message;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record DonationMessage(
        @NotNull String donationType,
        @NotNull String channelId,
        @NotNull String donatorChannelId,
        @NotNull String donatorNickname,
        @NotNull String payAmount,
        @NotNull String donationText,
        @NotNull Map<String, String> emojis
) {
}
