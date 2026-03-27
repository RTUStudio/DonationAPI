package kr.rtustudio.donation.service.soop.net.http.client;

import kr.rtustudio.donation.service.soop.net.http.client.impl.OkHttpSoopHttpClient;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

public interface SoopHttpClient<Client> {

    static @NotNull SoopHttpClient<OkHttpClient> okhttp() {
        return new OkHttpSoopHttpClient();
    }

    @NotNull Client getNativeHttpClient();

}
