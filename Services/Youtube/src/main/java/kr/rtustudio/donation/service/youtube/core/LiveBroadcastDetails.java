package kr.rtustudio.donation.service.youtube.core;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class LiveBroadcastDetails {
    @SerializedName("isLiveNow")
    @Expose
    public Boolean isLiveNow;

    @SerializedName("startTimestamp")
    @Expose
    public String startTimestamp;

    @SerializedName("endTimestamp")
    @Expose
    public String endTimestamp;
}
