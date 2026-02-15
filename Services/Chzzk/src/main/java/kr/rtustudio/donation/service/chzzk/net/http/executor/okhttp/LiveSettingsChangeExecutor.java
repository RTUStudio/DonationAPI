package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import kr.rtustudio.donation.service.chzzk.net.data.LiveSettingsChangeRequest;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.utils.Constants;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class LiveSettingsChangeExecutor implements HttpRequestExecutor<LiveSettingsChangeRequest, Void, OkHttpClient> {

    @Override
    public @NotNull Optional<Void> execute(@NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull LiveSettingsChangeRequest requestInst) {

        JsonObject json = new JsonObject();
        json.addProperty("defaultLiveTitle", requestInst.defaultLiveTitle());
        json.addProperty("categoryType", requestInst.categoryType());
        json.addProperty("categoryId", requestInst.categoryId());

        JsonArray arr = new JsonArray();
        requestInst.tags().forEach(arr::add);
        json.add("tags", arr);

        RequestBody body = RequestBody.create(Constants.MEDIA_TYPE_JSON, json.toString());

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/open/v1/lives/setting")
                .patch(body)
                .addHeader("Authorization", "Bearer " + requestInst.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return Optional.empty();
        } catch (IOException e) {
            throw new RuntimeException("Failed to execute LiveSettingsChange request", e);
        }
    }

}
