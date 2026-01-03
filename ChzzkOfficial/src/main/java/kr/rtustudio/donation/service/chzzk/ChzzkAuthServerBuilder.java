package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import org.jetbrains.annotations.NotNull;

public interface ChzzkAuthServerBuilder {

    @NotNull ChzzkAuthServerBuilder clientId(@NotNull String clientId);

    @NotNull ChzzkAuthServerBuilder clientSecret(@NotNull String clientSecret);

    @NotNull ChzzkAuthServerBuilder baseUri(@NotNull String baseUri);

    @NotNull ChzzkAuthServerBuilder host(@NotNull String host);

    @NotNull ChzzkAuthServerBuilder port(int port);

    @NotNull ChzzkAuthServerBuilder eventHandler(@NotNull ChzzkEventHandler handler);

    @NotNull ChzzkAuthServer build();

}
