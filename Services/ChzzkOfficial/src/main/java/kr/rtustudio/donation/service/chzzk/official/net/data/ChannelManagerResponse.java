package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ChannelManagerResponse(
        @NotNull List<Data> data
) {

    public record Data(
            @NotNull String managerChannelId,
            @NotNull String managerChannelName,
            @NotNull String userRole,
            @NotNull String createdDate
    ) {
    }

}
