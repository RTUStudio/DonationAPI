package kr.rtustudio.donation.service.chzzk.net.data.message;

import org.jetbrains.annotations.NotNull;

public record EventSubscribedMessage(
        @NotNull String type,
        @NotNull Data data
) {

    public record Data(
            @NotNull String eventType,
            @NotNull String channelId
    ) {
    }

}
