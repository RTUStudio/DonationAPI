package kr.rtustudio.donation.service.soop.net.http.executor.okhttp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.soop.net.data.ChatInfoRequest;
import kr.rtustudio.donation.service.soop.net.data.ChatInfoResponse;
import kr.rtustudio.donation.service.soop.net.http.client.SoopHttpClient;
import kr.rtustudio.donation.service.soop.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.soop.utils.Constants;
import lombok.extern.slf4j.Slf4j;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

@Slf4j(topic = "DonationAPI/SOOP")
public class ChatInfoExecutor implements HttpRequestExecutor<ChatInfoRequest, ChatInfoResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<ChatInfoResponse> execute(
            @NotNull SoopHttpClient<OkHttpClient> client, @NotNull ChatInfoRequest requestInst) {

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/broad/access/chatinfo")
                .post(new FormBody.Builder()
                        .add("access_token", requestInst.accessToken())
                        .build())
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            if (response.body() == null) {
                log.warn("ChatInfo response body is null, status={}", response.code());
                return Optional.empty();
            }

            String responseBody = response.body().string();
            log.debug("ChatInfo response: status={}, body={}", response.code(), responseBody);

            if (!response.isSuccessful()) {
                log.warn("ChatInfo request failed: status={}, body={}", response.code(), responseBody);
                return Optional.empty();
            }

            JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

            int result = json.has("result") ? json.get("result").getAsInt() : -1;
            if (result <= 0 || !json.has("data")) {
                log.warn("ChatInfo invalid result: result={}, msg={}", result, json.has("msg") ? json.get("msg").getAsString() : "N/A");
                return Optional.empty();
            }

            JsonArray data = json.getAsJsonArray("data");
            if (data.isEmpty()) {
                log.warn("ChatInfo data array is empty");
                return Optional.empty();
            }

            ChatInfoResponse chatInfo = Constants.GSON.fromJson(data.get(0), new TypeToken<ChatInfoResponse>() {}.getType());
            return Optional.ofNullable(chatInfo);
        } catch (IOException e) {
            log.warn("Failed to execute ChatInfo request: {}", e.getMessage());
            return Optional.empty();
        }
    }

}
