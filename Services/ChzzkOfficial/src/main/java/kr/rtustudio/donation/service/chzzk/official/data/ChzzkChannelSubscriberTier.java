package kr.rtustudio.donation.service.chzzk.official.data;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public enum ChzzkChannelSubscriberTier {

    TIER_1,
    TIER_2;

    public static @NotNull ChzzkChannelSubscriberTier from(@Range(from = 1, to = 2) int tier) {
        if (tier == 2) {
            return TIER_2;
        }
        return TIER_1;
    }

}
