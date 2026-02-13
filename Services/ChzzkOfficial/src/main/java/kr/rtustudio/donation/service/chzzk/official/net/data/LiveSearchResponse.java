package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record LiveSearchResponse(
        @NotNull List<Data> data,
        @NotNull Page page
) {

    public record Data(
            int liveId,
            @NotNull String liveTitle,
            @NotNull String liveThumbnailImageUrl,
            int concurrentUserCount,
            @NotNull String openDate,
            boolean adult,
            @NotNull List<String> tags,
            @NotNull String categoryType,
            @NotNull String liveCategory,
            @NotNull String liveCategoryValue,
            @NotNull String channelId,
            @NotNull String channelName,
            @NotNull String channelImageUrl
    ) {
    }

    public record Page(
            @NotNull String next
    ) {
    }

}
