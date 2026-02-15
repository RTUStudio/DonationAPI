package kr.rtustudio.donation.service.soop.net.http.client;

import kr.rtustudio.donation.service.soop.net.http.client.impl.OkHttpSOOPHttpClient;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

public interface SOOPHttpClient<Client> {

    static @NotNull SOOPHttpClient<OkHttpClient> okhttp() {
        return new OkHttpSOOPHttpClient();
    }

    @NotNull Client getNativeHttpClient();

}
