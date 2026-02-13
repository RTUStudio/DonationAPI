package kr.rtustudio.donation.service.chzzk.official.data;

import org.jetbrains.annotations.NotNull;

public enum ChzzkCategoryType {

    GAME, SPORTS, ETC;

    public static @NotNull ChzzkCategoryType fromString(@NotNull String str) {
        try {
            return valueOf(str.toUpperCase());
        } catch (Exception e) {
            return ETC;
        }
    }

}
