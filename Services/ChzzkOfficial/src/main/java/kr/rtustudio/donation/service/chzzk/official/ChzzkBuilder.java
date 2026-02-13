package kr.rtustudio.donation.service.chzzk.official;

import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.chzzk.official.data.ChzzkToken;
import kr.rtustudio.donation.service.chzzk.official.event.ChzzkEventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ChzzkBuilder {

    @NotNull ChzzkBuilder clientId(@NotNull String clientId);

    @NotNull ChzzkBuilder clientSecret(@NotNull String clientSecret);

    @NotNull ChzzkBuilder token(@Nullable ChzzkToken token);

    @NotNull ChzzkBuilder socketOption(@Nullable SocketOption socketOption);

    @NotNull ChzzkBuilder addEventHandler(@NotNull ChzzkEventHandler... handlers);

    @NotNull ChzzkBuilder addEventHandler(@NotNull Collection<ChzzkEventHandler> handlers);

    @NotNull Chzzk build();

}
