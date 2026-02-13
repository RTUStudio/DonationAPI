package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record LiveSettingsResponse(
        @NotNull String defaultLiveTitle,
        @NotNull Category category,
        @NotNull List<String> tags
) {

    public record Category(
            @NotNull String categoryType,
            @NotNull String categoryId,
            @NotNull String categoryValue,
            @NotNull String posterImageUrl
    ) {
    }

}
