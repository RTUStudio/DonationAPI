package kr.rtustudio.donation.service.soop.net.socket;

import kr.rtustudio.donation.service.soop.data.SOOPDonationMessage;
import org.jetbrains.annotations.NotNull;

public interface SOOPChatSocketHandler {

    default void onConnected(@NotNull SOOPChatSocket socket) {}

    default void onJoined(@NotNull SOOPChatSocket socket) {}

    default void onDonation(@NotNull SOOPChatSocket socket, @NotNull String action, @NotNull SOOPDonationMessage message) {}

    default void onDisconnected(@NotNull SOOPChatSocket socket) {}

    default void onError(@NotNull SOOPChatSocket socket, @NotNull Throwable error) {}

}
