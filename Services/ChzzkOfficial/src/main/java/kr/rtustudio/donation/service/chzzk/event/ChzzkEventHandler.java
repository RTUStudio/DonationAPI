package kr.rtustudio.donation.service.chzzk.event;

import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.data.ChzzkChatMessage;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Flow;

public interface ChzzkEventHandler {

    @NotNull Flow.Publisher<ChzzkEvent<?>> publisher();

    default void onUserRegistered(@NotNull Chzzk chzzk, @Nullable String user) {
    }

    default void onUserRegisterFailed(@NotNull String reason, @Nullable Throwable throwable) {
    }

    default void onGrantToken(@NotNull Chzzk chzzk) {
    }

    default void onRefreshToken(@NotNull Chzzk chzzk) {
    }

    default void onRevokeToken(@NotNull Chzzk chzzk) {
    }

    default void onChatMessage(@NotNull Chzzk chzzk, @NotNull ChzzkChatMessage message) {
    }

    default void onDonationMessage(@NotNull Chzzk chzzk, @NotNull ChzzkDonationMessage message) {
    }

}
