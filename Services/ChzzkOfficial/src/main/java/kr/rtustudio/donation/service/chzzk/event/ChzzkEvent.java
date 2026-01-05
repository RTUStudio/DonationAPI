package kr.rtustudio.donation.service.chzzk.event;

import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.data.ChzzkChatMessage;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationMessage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public record ChzzkEvent<T>(@NotNull ChzzkEventType type, @NotNull Chzzk source, @Nullable T payload) {

    public static @NotNull ChzzkEvent<Optional<String>> userRegistered(@NotNull Chzzk source, @Nullable String user) {
        return new ChzzkEvent<>(ChzzkEventType.USER_REGISTERED, source, Optional.ofNullable(user));
    }

    public static @NotNull ChzzkEvent<Void> tokenGranted(@NotNull Chzzk source) {
        return new ChzzkEvent<>(ChzzkEventType.TOKEN_GRANTED, source, null);
    }

    public static @NotNull ChzzkEvent<Void> tokenRefreshed(@NotNull Chzzk source) {
        return new ChzzkEvent<>(ChzzkEventType.TOKEN_REFRESHED, source, null);
    }

    public static @NotNull ChzzkEvent<Void> tokenRevoked(@NotNull Chzzk source) {
        return new ChzzkEvent<>(ChzzkEventType.TOKEN_REVOKED, source, null);
    }

    public static @NotNull ChzzkEvent<ChzzkChatMessage> chatMessage(@NotNull Chzzk source, @NotNull ChzzkChatMessage payload) {
        return new ChzzkEvent<>(ChzzkEventType.CHAT_MESSAGE, source, payload);
    }

    public static @NotNull ChzzkEvent<ChzzkDonationMessage> donationMessage(@NotNull Chzzk source, @NotNull ChzzkDonationMessage payload) {
        return new ChzzkEvent<>(ChzzkEventType.DONATION_MESSAGE, source, payload);
    }

}