package kr.rtustudio.donation.service.chzzk.official.net.http.executor.okhttp;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import kr.rtustudio.donation.service.chzzk.official.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.official.net.data.ChatMessageSendRequest;
import kr.rtustudio.donation.service.chzzk.official.net.data.ChatMessageSendResponse;
import kr.rtustudio.donation.service.chzzk.official.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.official.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.official.utils.Constants;
import kr.rtustudio.donation.service.chzzk.official.utils.HttpResponseParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class ChatMessageSendExecutor implements HttpRequestExecutor<ChatMessageSendRequest, ChatMessageSendResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<ChatMessageSendResponse> execute(
            @NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull ChatMessageSendRequest requestInst) {

        JsonObject json = new JsonObject();
        json.addProperty("message", requestInst.message());

        RequestBody body = RequestBody.create(Constants.MEDIA_TYPE_JSON, json.toString());

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/open/v1/chats/send")
                .post(body)
                .addHeader("Authorization", "Bearer " + requestInst.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return HttpResponseParser.parse(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            throw new RuntimeException("Failed to execute ChatMessageSend request", e);
        }
    }

}
