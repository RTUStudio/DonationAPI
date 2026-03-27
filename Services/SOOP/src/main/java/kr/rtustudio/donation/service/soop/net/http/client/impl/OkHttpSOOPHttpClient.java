package kr.rtustudio.donation.service.soop.net.http.client.impl;

import kr.rtustudio.donation.service.soop.net.http.client.SoopHttpClient;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

public class OkHttpSoopHttpClient implements SoopHttpClient<OkHttpClient> {

    private final @NotNull OkHttpClient client;

    public OkHttpSoopHttpClient() {
        this.client = new OkHttpClient();
    }

    @Override
    public @NotNull OkHttpClient getNativeHttpClient() {
        return client;
    }

}
