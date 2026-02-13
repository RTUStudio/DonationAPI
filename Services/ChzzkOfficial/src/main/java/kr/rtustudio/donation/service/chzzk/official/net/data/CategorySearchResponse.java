package kr.rtustudio.donation.service.chzzk.official.net.data;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record CategorySearchResponse(
        @NotNull List<Data> data

) {

    public record Data(
            @NotNull String categoryType,
            @NotNull String categoryId,
            @NotNull String categoryValue,
            @NotNull String posterImageUrl
    ) {
    }

}
