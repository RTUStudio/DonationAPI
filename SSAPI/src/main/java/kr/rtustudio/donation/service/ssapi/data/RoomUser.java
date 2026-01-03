package kr.rtustudio.donation.service.ssapi.data;

import com.google.gson.annotations.SerializedName;

public record RoomUser(
        @SerializedName("platform") String platform,
        @SerializedName("streamer_id") String streamerId,
        @SerializedName("createdAt") String createdAt
) {
}
