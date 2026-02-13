package kr.rtustudio.donation.service.chzzk.official.data;

import kr.rtustudio.donation.service.chzzk.official.net.data.message.DonationMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ChzzkDonationMessage(
        @NotNull ChzzkDonationType donationType,
        @NotNull String receiverChannelId,
        @NotNull String senderChannelId,
        @NotNull String nickname,
        @NotNull String message,
        int payAmount,
        @NotNull Map<String, String> emojis
) {

    public static @NotNull ChzzkDonationMessage of(@NotNull DonationMessage message) {
        return new ChzzkDonationMessage(
                ChzzkDonationType.fromString(message.donationType()), message.channelId(),
                message.donatorChannelId(), message.donatorNickname(), message.donationText(),
                Integer.parseInt(message.payAmount()), message.emojis()
        );
    }

}
