package kr.rtustudio.donation.service.chzzk.official.net.http.client.impl;

import kr.rtustudio.donation.service.chzzk.official.net.http.client.ChzzkHttpClient;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

public class OkHttpChzzkHttpClient implements ChzzkHttpClient<OkHttpClient> {

    private final @NotNull OkHttpClient client;

    public OkHttpChzzkHttpClient() {
        this.client = new OkHttpClient();
    }

    @Override
    public @NotNull OkHttpClient getNativeHttpClient() {
        return client;
    }

}
