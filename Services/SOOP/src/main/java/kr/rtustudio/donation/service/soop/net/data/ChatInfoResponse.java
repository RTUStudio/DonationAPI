package kr.rtustudio.donation.service.soop.net.data;

import com.google.gson.annotations.SerializedName;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record ChatInfoResponse(
        @SerializedName("chat_ip") @NotNull String chatIp,
        @SerializedName("chat_port") int chatPort,
        @SerializedName("id") @NotNull String bjId,
        @SerializedName("nick") @Nullable String bjNickname,
        @SerializedName("broad_no") int broadNo,
        @SerializedName("chat_no") int chatNo,
        @SerializedName("key") @NotNull String ticket,
        @SerializedName("title") @Nullable String title,
        @SerializedName("cate_name") @Nullable String category,
        @SerializedName("broad_type") @Nullable String broadType
) {
}
