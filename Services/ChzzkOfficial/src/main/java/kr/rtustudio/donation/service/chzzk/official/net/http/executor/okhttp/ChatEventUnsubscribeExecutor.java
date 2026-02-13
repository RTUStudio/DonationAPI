package kr.rtustudio.donation.service.chzzk.official.net.http.executor.okhttp;


import kr.rtustudio.donation.service.chzzk.official.net.data.ChatEventUnsubscribeRequest;
import kr.rtustudio.donation.service.chzzk.official.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.official.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.official.utils.Constants;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class ChatEventUnsubscribeExecutor implements HttpRequestExecutor<ChatEventUnsubscribeRequest, Void, OkHttpClient> {

    @Override
    public @NotNull Optional<Void> execute(@NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull ChatEventUnsubscribeRequest requestInst) {
        RequestBody body = new FormBody.Builder()
                .add("sessionKey", requestInst.sessionKey())
                .build();

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/open/v1/sessions/events/unsubscribe/chat")
                .post(body)
                .addHeader("Authorization", "Bearer " + requestInst.accessToken())
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute ChatEventUnsubscribe request", e);
        }
    }

}
