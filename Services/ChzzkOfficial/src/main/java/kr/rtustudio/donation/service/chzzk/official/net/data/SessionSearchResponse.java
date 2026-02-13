package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SessionSearchResponse(
        @NotNull List<Data> data
) {

    public record Data(
            @NotNull String sessionKey,
            @NotNull String connectedDate,
            @NotNull String disconnectedDate,
            @NotNull List<Event> subscribedEvents
    ) {
    }

    public record Event(
            @NotNull String eventType,
            @NotNull String channelId
    ) {
    }

}
