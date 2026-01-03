package kr.rtustudio.donation.service.chzzk.data;

import com.google.common.collect.Lists;
import kr.rtustudio.donation.service.chzzk.net.data.SessionSearchResponse;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public record ChzzkSessionSearchResult(
        @NotNull String sessionKey,
        @NotNull String connectedData,
        @NotNull String disconnectedDate,
        @NotNull List<Event> events
) {

    public static @NotNull ChzzkSessionSearchResult of(@NotNull SessionSearchResponse.Data response) {
        return of(
                response.sessionKey(), response.connectedDate(),
                response.disconnectedDate(), Event.of(response.subscribedEvents())
        );
    }

    public static @NotNull ChzzkSessionSearchResult of(
            @NotNull String sessionKey, @NotNull String connectedData,
            @NotNull String disconnectedDate, @NotNull List<Event> events
    ) {
        return new ChzzkSessionSearchResult(sessionKey, connectedData, disconnectedDate, events);
    }

    public record Event(
            @NotNull ChzzkEventType type,
            @NotNull String channelId
    ) {

        public static @NotNull List<Event> of(@NotNull Collection<SessionSearchResponse.Event> response) {
            List<Event> events = Lists.newArrayList();
            response.forEach(it -> {
                Event event = of(it);
                events.add(event);
            });
            return events;
        }

        public static @NotNull Event of(@NotNull SessionSearchResponse.Event event) {
            return of(event.eventType(), event.channelId());
        }

        public static @NotNull Event of(@NotNull String type, @NotNull String channelId) {
            return new Event(ChzzkEventType.fromString(type), channelId);
        }

    }

}
