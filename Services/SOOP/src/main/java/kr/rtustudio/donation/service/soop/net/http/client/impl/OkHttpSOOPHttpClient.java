package kr.rtustudio.donation.service.soop.net.http.client.impl;

import kr.rtustudio.donation.service.soop.net.http.client.SOOPHttpClient;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

public class OkHttpSOOPHttpClient implements SOOPHttpClient<OkHttpClient> {

    private final @NotNull OkHttpClient client;

    public OkHttpSOOPHttpClient() {
        this.client = new OkHttpClient();
    }

    @Override
    public @NotNull OkHttpClient getNativeHttpClient() {
        return client;
    }

}
