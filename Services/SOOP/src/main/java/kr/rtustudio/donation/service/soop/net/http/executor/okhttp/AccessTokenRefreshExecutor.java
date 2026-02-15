package kr.rtustudio.donation.service.soop.net.http.executor.okhttp;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.soop.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.soop.net.data.AccessTokenRefreshRequest;
import kr.rtustudio.donation.service.soop.net.data.AccessTokenRefreshResponse;
import kr.rtustudio.donation.service.soop.net.http.client.SOOPHttpClient;
import kr.rtustudio.donation.service.soop.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.soop.utils.Constants;
import kr.rtustudio.donation.service.soop.utils.HttpResponseParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class AccessTokenRefreshExecutor implements HttpRequestExecutor<AccessTokenRefreshRequest, AccessTokenRefreshResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<AccessTokenRefreshResponse> execute(
            @NotNull SOOPHttpClient<OkHttpClient> client, @NotNull AccessTokenRefreshRequest requestInst) {

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("grant_type", requestInst.grantType());
        requestJson.addProperty("client_id", requestInst.clientId());
        requestJson.addProperty("client_secret", requestInst.clientSecret());
        requestJson.addProperty("refresh_token", requestInst.refreshToken());

        RequestBody body = RequestBody.create(Constants.MEDIA_TYPE_JSON, requestJson.toString());

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/auth/token")
                .post(body)
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return HttpResponseParser.parseFlat(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            throw new RuntimeException(e);
        }
    }

}
