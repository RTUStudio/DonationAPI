package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import kr.rtustudio.donation.service.chzzk.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.net.data.RestrictionAddRequest;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.utils.Constants;
import kr.rtustudio.donation.service.chzzk.utils.HttpResponseParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class RestrictionAddExecutor implements HttpRequestExecutor<RestrictionAddRequest, Void, OkHttpClient> {

    @Override
    public @NotNull Optional<Void> execute(@NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull RestrictionAddRequest requestInst) {
        JsonObject json = new JsonObject();
        json.addProperty("targetChannelId", requestInst.targetChannelId());

        RequestBody body = RequestBody.create(Constants.MEDIA_TYPE_JSON, json.toString());

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/open/v1/restrict-channels")
                .post(body)
                .addHeader("Authorization", "Bearer " + requestInst.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return HttpResponseParser.parse(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            throw new RuntimeException("Failed to execute RestrictionAdd request", e);
        }
    }

}
