package kr.rtustudio.donation.service.chzzk.official.event;

import kr.rtustudio.donation.service.chzzk.official.Chzzk;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkChatMessage;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkDonationMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ChzzkEventHandler {

    default void onUserRegistered(@NotNull Chzzk chzzk, @Nullable String user) {}

    default void onGrantToken(@NotNull Chzzk chzzk) {}

    default void onRefreshToken(@NotNull Chzzk chzzk) {}

    default void onRevokeToken(@NotNull Chzzk chzzk) {}

    default void onChatMessage(@NotNull Chzzk chzzk, @NotNull ChzzkChatMessage message) {}

    default void onDonationMessage(@NotNull Chzzk chzzk, @NotNull ChzzkDonationMessage message) {}

}
