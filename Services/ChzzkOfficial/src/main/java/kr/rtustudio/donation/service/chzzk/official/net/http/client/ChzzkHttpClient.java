package kr.rtustudio.donation.service.chzzk.official.net.http.client;

import kr.rtustudio.donation.service.chzzk.official.net.http.client.impl.OkHttpChzzkHttpClient;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

public interface ChzzkHttpClient<Client> {

    static @NotNull ChzzkHttpClient<OkHttpClient> okhttp() {
        return new OkHttpChzzkHttpClient();
    }

    @NotNull Client getNativeHttpClient();

}
