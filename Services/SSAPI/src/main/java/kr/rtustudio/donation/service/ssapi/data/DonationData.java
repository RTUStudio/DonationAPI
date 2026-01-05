package kr.rtustudio.donation.service.ssapi.data;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public record DonationData(
        @SerializedName("_id") String id,
        @SerializedName("platform") String platform,
        @SerializedName("type") String type,
        @SerializedName("streamer_id") String streamerId,
        @SerializedName("user_id") String userId,
        @SerializedName("nickname") String nickname,
        @SerializedName("cnt") int count,
        @SerializedName("message") String message,
        @SerializedName("amount") int amount,
        @SerializedName("extras") Map<String, Object> extras
) {
}
