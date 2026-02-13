package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

public record ChatSettingsResponse(
        @NotNull String chatAvailableCondition,
        @NotNull String chatAvailableGroup,
        int minFollowerMinute,
        boolean allowSubscriberInFollowerMode
) {
}
