package kr.rtustudio.donation.service.soop.net.socket;

import kr.rtustudio.donation.service.soop.data.SoopDonationMessage;
import org.jetbrains.annotations.NotNull;

public interface SoopChatSocketHandler {

    default void onConnected(@NotNull SoopChatSocket socket) {}

    default void onJoined(@NotNull SoopChatSocket socket) {}

    default void onDonation(@NotNull SoopChatSocket socket, @NotNull String action, @NotNull SoopDonationMessage message) {}

    default void onDisconnected(@NotNull SoopChatSocket socket) {}

    default void onError(@NotNull SoopChatSocket socket, @NotNull Throwable error) {}

}
