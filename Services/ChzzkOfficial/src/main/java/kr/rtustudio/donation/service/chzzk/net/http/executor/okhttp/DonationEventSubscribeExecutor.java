package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import kr.rtustudio.donation.service.chzzk.exception.HttpRequestExecutionException;
import kr.rtustudio.donation.service.chzzk.net.data.DonationEventSubscribeRequest;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.utils.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class DonationEventSubscribeExecutor implements HttpRequestExecutor<DonationEventSubscribeRequest, Void, OkHttpClient> {

    @Override
    public @NotNull Optional<Void> execute(@NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull DonationEventSubscribeRequest requestInst) throws HttpRequestExecutionException {
        RequestBody body = new FormBody.Builder()
                .add("sessionKey", requestInst.sessionKey())
                .build();

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/open/v1/sessions/events/subscribe/donation")
                .post(body)
                .addHeader("Authorization", "Bearer " + requestInst.accessToken())
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return Optional.empty();
        } catch (IOException e) {
            throw new HttpRequestExecutionException("Failed to execute DonationEventSubscribe request", e);
        }
    }

}
