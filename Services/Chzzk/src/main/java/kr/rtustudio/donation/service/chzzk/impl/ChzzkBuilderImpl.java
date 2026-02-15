package kr.rtustudio.donation.service.chzzk.impl;

import com.google.common.collect.Sets;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.ChzzkBuilder;
import kr.rtustudio.donation.service.chzzk.data.ChzzkToken;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ChzzkBuilderImpl implements ChzzkBuilder {

    private String clientId;
    private String clientSecret;
    private ChzzkToken token;
    private SocketOption socketOption;
    private Set<ChzzkEventHandler> handlers = Sets.newHashSet();

    public ChzzkBuilderImpl() {
    }

    public @NotNull ChzzkBuilder clientId(@NotNull String clientId) {
        this.clientId = clientId;
        return this;
    }

    public @NotNull ChzzkBuilder clientSecret(@NotNull String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public @NotNull ChzzkBuilder token(@Nullable ChzzkToken token) {
        this.token = token;
        return this;
    }

    @Override
    public @NotNull ChzzkBuilder socketOption(@Nullable SocketOption socketOption) {
        this.socketOption = socketOption;
        return this;
    }

    @Override
    public @NotNull ChzzkBuilder addEventHandler(@NotNull ChzzkEventHandler... handlers) {
        Collections.addAll(this.handlers, handlers);
        return this;
    }

    @Override
    public @NotNull ChzzkBuilder addEventHandler(@NotNull Collection<ChzzkEventHandler> handlers) {
        this.handlers.addAll(handlers);
        return this;
    }

    public @NotNull Chzzk build() {
        if (clientId == null || clientSecret == null) {
            throw new IllegalArgumentException("Missing required fields.");
        }

        return new ChzzkImpl(clientId, clientSecret, token, socketOption, handlers);
    }

}
