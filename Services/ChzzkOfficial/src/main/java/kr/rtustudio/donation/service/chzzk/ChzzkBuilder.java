package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.service.chzzk.data.ChzzkToken;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ChzzkBuilder {

    @NotNull ChzzkBuilder clientId(@NotNull String clientId);

    @NotNull ChzzkBuilder clientSecret(@NotNull String clientSecret);

    @NotNull ChzzkBuilder token(@Nullable ChzzkToken token);

    @NotNull ChzzkBuilder eventHandler(@NotNull ChzzkEventHandler handler);

    @NotNull Chzzk build();

}
