package kr.rtustudio.donation.service.chzzk.net.data.message;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record ChatMessage(
        @NotNull String channelId,
        @NotNull String senderChannelId,
        @NotNull Profile profile,
        @NotNull String content,
        @NotNull Map<String, String> emojis,
        double messageTime
) {

    public record Profile(
            @NotNull String nickname,
            @NotNull List<Badge> badges,
            boolean verifiedMark
    ) {
    }

    public record Badge(
            @NotNull String imageUrl
    ) {
    }

}
