package kr.rtustudio.donation.service.chzzk.data;

import kr.rtustudio.donation.service.chzzk.net.data.message.ChatMessage;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record ChzzkChatMessage(
        @NotNull String receiverChannelId,
        @NotNull String senderChannelId,
        @NotNull String nickname,
        boolean hasVerifiedMark,
        @NotNull String message,
        @NotNull Map<String, String> emojis,
        double messageTime
) {

    public static @NotNull ChzzkChatMessage of(@NotNull ChatMessage message) {
        return new ChzzkChatMessage(
                message.channelId(), message.senderChannelId(), message.profile().nickname(),
                message.profile().verifiedMark(), message.content(), message.emojis(),
                message.messageTime()
        );
    }

}
