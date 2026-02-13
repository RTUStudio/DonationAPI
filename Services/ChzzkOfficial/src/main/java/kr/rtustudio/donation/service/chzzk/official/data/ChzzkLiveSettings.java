package kr.rtustudio.donation.service.chzzk.official.data;

import kr.rtustudio.donation.service.chzzk.official.net.data.LiveSettingsResponse;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record ChzzkLiveSettings(
        @NotNull String liveTitle,
        @NotNull ChzzkCategoryType categoryType,
        @NotNull String categoryId,
        @NotNull String categoryName,
        @NotNull String categoryPosterImageUrl,
        @NotNull List<String> tags
) {

    public static @NotNull ChzzkLiveSettings of(@NotNull LiveSettingsResponse response) {
        return of(
                response.defaultLiveTitle(), ChzzkCategoryType.fromString(response.category().categoryType()),
                response.category().categoryId(), response.category().categoryValue(),
                response.category().posterImageUrl(), response.tags()
        );
    }

    public static @NotNull ChzzkLiveSettings of(
            @NotNull String title, @NotNull ChzzkCategoryType categoryType,
            @NotNull String categoryId, @NotNull List<String> tags
    ) {
        return of(title, categoryType, categoryId, "", "", tags);
    }

    public static @NotNull ChzzkLiveSettings of(
            @NotNull String liveTitle, @NotNull ChzzkCategoryType categoryType,
            @NotNull String categoryId, @NotNull String categoryName,
            @NotNull String categoryPosterImageUrl, @NotNull List<String> tags
    ) {
        return new ChzzkLiveSettings(liveTitle, categoryType, categoryId, categoryName, categoryPosterImageUrl, tags);
    }

}
