package kr.rtustudio.donation.service.chzzk.official.data;

import org.jetbrains.annotations.NotNull;

public enum ChzzkChatAvailableGroup {

    ALL, FOLLOWER, MANAGER, SUBSCRIBER;

    public static @NotNull ChzzkChatAvailableGroup fromString(@NotNull String str) {
        try {
            return valueOf(str.toUpperCase());
        } catch (Exception e) {
            return ALL;
        }
    }

}
