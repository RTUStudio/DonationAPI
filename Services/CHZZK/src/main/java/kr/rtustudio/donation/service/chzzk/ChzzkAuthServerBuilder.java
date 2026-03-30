package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

public interface ChzzkAuthServerBuilder {

    @NotNull ChzzkAuthServerBuilder clientId(@NotNull String clientId);

    @NotNull ChzzkAuthServerBuilder clientSecret(@NotNull String clientSecret);

    @NotNull ChzzkAuthServerBuilder baseUri(@NotNull String baseUri);

    @NotNull ChzzkAuthServerBuilder host(@NotNull String host);

    @NotNull ChzzkAuthServerBuilder port(int port);

    @NotNull ChzzkAuthServerBuilder socketOption(@Nullable SocketOption socketOption);

    @NotNull ChzzkAuthServerBuilder addChzzkEventHandler(@NotNull ChzzkEventHandler... handlers);

    @NotNull ChzzkAuthServerBuilder addChzzkEventHandler(@NotNull Collection<ChzzkEventHandler> handlers);

    @NotNull ChzzkAuthServer build();

}
