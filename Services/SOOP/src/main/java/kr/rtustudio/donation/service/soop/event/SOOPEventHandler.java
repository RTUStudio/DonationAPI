package kr.rtustudio.donation.service.soop.event;

import kr.rtustudio.donation.service.soop.data.SOOPDonationMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SOOPEventHandler {

    default boolean onUserRegistered(@NotNull String bjId, @Nullable String user) { return true; }

    default void onDonationMessage(@NotNull String bjId, @NotNull SOOPDonationMessage message) {}

}
