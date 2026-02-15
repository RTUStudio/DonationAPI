package kr.rtustudio.donation.service.chzzk.data;

import org.jetbrains.annotations.NotNull;

public enum ChzzkEventType {

    CHAT, DONATION;

    public static @NotNull ChzzkEventType fromString(@NotNull String str) {
        try {
            return valueOf(str.toUpperCase());
        } catch (Exception e) {
            return CHAT;
        }
    }

}
