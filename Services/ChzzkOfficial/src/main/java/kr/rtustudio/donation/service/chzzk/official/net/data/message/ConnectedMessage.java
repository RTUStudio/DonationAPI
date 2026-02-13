package kr.rtustudio.donation.service.chzzk.official.net.data.message;

import org.jetbrains.annotations.NotNull;

public record ConnectedMessage(
        @NotNull String type,
        @NotNull Data data
) {

    public record Data(
            @NotNull String sessionKey
    ) {
    }

}
