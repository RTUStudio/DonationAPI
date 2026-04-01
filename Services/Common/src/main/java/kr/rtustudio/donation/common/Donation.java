package kr.rtustudio.donation.common;

import kr.rtustudio.donation.service.Services;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public record Donation(
        @Nullable UUID uniqueId,
        @NotNull Services service,
        @NotNull Platform platform,
        @Nullable DonationType type,
        @Nullable String streamer,
        @Nullable String donator,
        @Nullable String nickname,
        @Nullable String message,
        int amount
) {
    public Donation {
        type = type != null ? type : DonationType.CHAT;
        streamer = streamer != null ? streamer : "";
        donator = donator != null ? donator : "";
        nickname = nickname != null ? nickname : "익명의 후원자";
        message = message != null ? message : "";
    }

    /**
     * 플랫폼 후원 단위명을 반환합니다.
     *
     * @return 단위명 (개, 치즈, 원, 캐시, 빔)
     */
    public String unit() {
        return platform.unit();
    }

    /**
     * 총 금액(원)을 반환합니다. (amount × platform.rate())
     *
     * @return 총 후원 금액 (원)
     */
    public int price() {
        return amount * platform.rate();
    }
}
