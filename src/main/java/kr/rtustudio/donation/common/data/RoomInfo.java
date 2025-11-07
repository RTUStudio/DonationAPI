package kr.rtustudio.donation.common.data;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public record RoomInfo(
        @SerializedName("id") String id,
        @SerializedName("users") List<RoomUser> users,
        @SerializedName("users_limit") int usersLimit,
        @SerializedName("createdAt") String createdAt,
        @SerializedName("updatedAt") String updatedAt
) {
}
