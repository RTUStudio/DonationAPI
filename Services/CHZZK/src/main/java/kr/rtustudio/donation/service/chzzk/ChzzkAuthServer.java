package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.service.chzzk.impl.ChzzkAuthServerBuilderImpl;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

public interface ChzzkAuthServer {

    static @NotNull ChzzkAuthServerBuilder builder() {
        return new ChzzkAuthServerBuilderImpl();
    }

    @NotNull String getClientId();

    @NotNull String getClientSecret();

    @NotNull String getBaseUri();

    @NotNull String getHost();

    @Range(from = 0, to = 65535)
    int getPort();

    void start();

    void stop();

}
