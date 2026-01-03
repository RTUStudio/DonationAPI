package kr.rtustudio.donation.service.chzzk.impl;

import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.ChzzkBuilder;
import kr.rtustudio.donation.service.chzzk.data.ChzzkToken;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChzzkBuilderImpl implements ChzzkBuilder {

    private String clientId;
    private String clientSecret;
    private ChzzkToken token;
    private ChzzkEventHandler handler;

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
    public @NotNull ChzzkBuilder eventHandler(@NotNull ChzzkEventHandler handler) {
        this.handler = handler;
        return this;
    }

    public @NotNull Chzzk build() {
        if (this.clientId == null || this.clientSecret == null || this.handler == null) {
            throw new IllegalArgumentException("Missing required fields.");
        }

        return new ChzzkImpl(this.clientId, this.clientSecret, this.token, this.handler);
    }

}
