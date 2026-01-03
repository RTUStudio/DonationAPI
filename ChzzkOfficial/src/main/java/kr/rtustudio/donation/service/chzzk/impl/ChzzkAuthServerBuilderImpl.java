package kr.rtustudio.donation.service.chzzk.impl;

import kr.rtustudio.donation.service.chzzk.ChzzkAuthServer;
import kr.rtustudio.donation.service.chzzk.ChzzkAuthServerBuilder;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import org.jetbrains.annotations.NotNull;

public class ChzzkAuthServerBuilderImpl implements ChzzkAuthServerBuilder {

    private String clientId;
    private String clientSecret;
    private String baseUri;
    private String host = "0.0.0.0";
    private int port = 80;

    private ChzzkEventHandler handler;

    @Override
    public @NotNull ChzzkAuthServerBuilder clientId(@NotNull String clientId) {
        this.clientId = clientId;
        return this;
    }

    @Override
    public @NotNull ChzzkAuthServerBuilder clientSecret(@NotNull String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    @Override
    public @NotNull ChzzkAuthServerBuilder baseUri(@NotNull String baseUri) {
        this.baseUri = baseUri;
        return this;
    }

    @Override
    public @NotNull ChzzkAuthServerBuilder host(@NotNull String host) {
        this.host = host;
        return this;
    }

    @Override
    public @NotNull ChzzkAuthServerBuilder port(int port) {
        this.port = port;
        return this;
    }

    @Override
    public @NotNull ChzzkAuthServerBuilder eventHandler(@NotNull ChzzkEventHandler handler) {
        this.handler = handler;
        return this;
    }

    @Override
    public @NotNull ChzzkAuthServer build() {
        if (this.clientId == null
                || this.clientSecret == null
                || this.baseUri == null
                || this.host == null
                || this.port < 0
                || this.port > 65535
                || this.handler == null
        ) {
            throw new IllegalArgumentException("Missing required fields.");
        }
        return new ChzzkAuthServerImpl(this.clientId, this.clientSecret, this.baseUri, this.host, this.port, this.handler);
    }

}
