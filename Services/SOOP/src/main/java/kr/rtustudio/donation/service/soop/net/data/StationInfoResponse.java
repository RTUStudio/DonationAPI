package kr.rtustudio.donation.service.soop.net.data;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record StationInfoResponse(
        @SerializedName("user_nick") @Nullable String userNick,
        @SerializedName("station_name") @Nullable String stationName,
        @SerializedName("profile_image") @Nullable String profileImage,
        @SerializedName("lately_broad_date") @Nullable String latelyBroadDate,
        @SerializedName("favorite_cnt") int favoriteCnt
) {
}
