package kr.rtustudio.donation.service.chzzk.data;

import kr.rtustudio.donation.service.chzzk.net.data.LiveSearchResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.ZonedDateTime;
import java.util.List;

public record ChzzkLiveSearchResult(
        @Range(from = 0, to = Integer.MAX_VALUE) int liveId,
        @NotNull String liveTitle,
        @NotNull String liveThumbnailImageUrl,
        @Range(from = 0, to = Integer.MAX_VALUE) int userCount,
        @NotNull ZonedDateTime liveStartTime,
        boolean isAdultMode,
        @NotNull List<String> tags,
        @NotNull ChzzkCategoryType categoryType,
        @NotNull String categoryId,
        @NotNull String categoryName,
        @NotNull String channelId,
        @NotNull String channelName,
        @NotNull String channelImageUrl,
        @NotNull String nextPage
) {

    public static @NotNull ChzzkLiveSearchResult of(@NotNull LiveSearchResponse.Data response, @NotNull LiveSearchResponse.Page page) {
        ZonedDateTime date = ZonedDateTime.parse(response.openDate());
        ChzzkCategoryType categoryType = ChzzkCategoryType.fromString(response.categoryType());
        return of(
                response.liveId(), response.liveTitle(), response.liveThumbnailImageUrl(),
                response.concurrentUserCount(), date,
                response.adult(), response.tags(), categoryType,
                response.liveCategory(), response.liveCategoryValue(), response.channelId(),
                response.channelName(), response.channelImageUrl(), page.next()
        );
    }

    public static @NotNull ChzzkLiveSearchResult of(
            @Range(from = 0, to = Integer.MAX_VALUE) int liveId, @NotNull String liveTitle, @NotNull String liveThumbnailImageUrl,
            @Range(from = 0, to = Integer.MAX_VALUE) int userCount, @NotNull ZonedDateTime liveStartTime,
            boolean adultMode, @NotNull List<String> tags, @NotNull ChzzkCategoryType categoryType,
            @NotNull String categoryId, @NotNull String categoryName, @NotNull String channelId,
            @NotNull String channelName, @NotNull String channelImageUrl, @NotNull String nextPage
    ) {
        return new ChzzkLiveSearchResult(
                liveId, liveTitle, liveThumbnailImageUrl, userCount,
                liveStartTime, adultMode, tags, categoryType, categoryId,
                categoryName, channelId, channelName, channelImageUrl, nextPage
        );
    }

}
