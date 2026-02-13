package kr.rtustudio.donation.service.chzzk.official.net.http.executor.okhttp;

import com.google.gson.JsonObject;

import kr.rtustudio.donation.service.chzzk.official.net.data.ChatAnnouncementSetRequest;
import kr.rtustudio.donation.service.chzzk.official.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.official.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.official.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class ChatAnnouncementSetExecutor implements HttpRequestExecutor<ChatAnnouncementSetRequest, Void, OkHttpClient> {

    @Override
    public @NotNull Optional<Void> execute(
            @NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull ChatAnnouncementSetRequest requestInst) {

        JsonObject json = new JsonObject();
        if (!requestInst.message().isEmpty()) {
            json.addProperty("message", requestInst.message());
        }
        if (!requestInst.messageId().isEmpty()) {
            json.addProperty("messageId", requestInst.messageId());
        }

        RequestBody body = RequestBody.create(Constants.MEDIA_TYPE_JSON, json.toString());

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/open/v1/chats/notice")
                .post(body)
                .addHeader("Authorization", "Bearer " + requestInst.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute ChatAnnouncementSet request", e);
        }
    }

}
