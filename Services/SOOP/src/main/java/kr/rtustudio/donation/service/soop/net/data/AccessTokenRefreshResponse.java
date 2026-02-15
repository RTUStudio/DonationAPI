package kr.rtustudio.donation.service.soop.net.data;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;

public record AccessTokenRefreshResponse(
        @SerializedName("access_token") @NotNull String accessToken,
        @SerializedName("refresh_token") @NotNull String refreshToken
) {
}
