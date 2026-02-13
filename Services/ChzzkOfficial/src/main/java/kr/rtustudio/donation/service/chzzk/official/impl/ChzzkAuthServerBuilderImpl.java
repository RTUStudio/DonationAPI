package kr.rtustudio.donation.service.chzzk.official.impl;

import com.google.common.collect.Sets;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.chzzk.official.ChzzkAuthServer;
import kr.rtustudio.donation.service.chzzk.official.ChzzkAuthServerBuilder;
import kr.rtustudio.donation.service.chzzk.official.event.ChzzkEventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ChzzkAuthServerBuilderImpl implements ChzzkAuthServerBuilder {

    private String clientId;
    private String clientSecret;
    private String baseUri;
    private String host = "0.0.0.0";
    private int port = 80;
    private SocketOption socketOption;

    private final Set<ChzzkEventHandler> handlers = Sets.newHashSet();

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
    public @NotNull ChzzkAuthServerBuilder socketOption(@Nullable SocketOption socketOption) {
        this.socketOption = socketOption;
        return this;
    }

    @Override
    public @NotNull ChzzkAuthServerBuilder addChzzkEventHandler(@NotNull ChzzkEventHandler... handlers) {
        Collections.addAll(this.handlers, handlers);
        return this;
    }

    @Override
    public @NotNull ChzzkAuthServerBuilder addChzzkEventHandler(@NotNull Collection<ChzzkEventHandler> handlers) {
        this.handlers.addAll(handlers);
        return this;
    }

    @Override
    public @NotNull ChzzkAuthServer build() {
        if (clientId == null
                || clientSecret == null
                || baseUri == null
                || host == null
                || port < 0
                || port > 65535
        ) {
            throw new IllegalArgumentException("Missing required fields.");
        }
        return new ChzzkAuthServerImpl(clientId, clientSecret, baseUri, host, port, socketOption, handlers);
    }

}
