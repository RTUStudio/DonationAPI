package kr.rtustudio.donation.service.chzzk.net.socket;

import kr.rtustudio.donation.service.chzzk.net.data.message.*;
import org.jetbrains.annotations.NotNull;

public interface SessionSocketHandler {

    default void onInvalidResponse(@NotNull SessionSocket session, @NotNull Object[] response) {
    }

    default void onConnected(@NotNull SessionSocket session, @NotNull ConnectedMessage message) {
    }

    default void onEventSubscribed(@NotNull SessionSocket session, @NotNull EventSubscribedMessage message) {
    }

    default void onEventUnsubscribed(@NotNull SessionSocket session, @NotNull EventUnsubscribedMessage message) {
    }

    default void onEventRevoked(@NotNull SessionSocket session, @NotNull EventRevokedMessage message) {
    }

    default void onChatMessageReceived(@NotNull SessionSocket session, @NotNull ChatMessage message) {
    }

    default void onDonationMessageReceived(@NotNull SessionSocket session, @NotNull DonationMessage message) {
    }

}
