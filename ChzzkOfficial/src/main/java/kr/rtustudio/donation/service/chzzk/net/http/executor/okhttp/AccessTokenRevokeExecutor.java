package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import com.google.gson.JsonObject;
import kr.rtustudio.donation.service.chzzk.exception.HttpRequestExecutionException;
import kr.rtustudio.donation.service.chzzk.net.data.AccessTokenRevokeRequest;
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

public class AccessTokenRevokeExecutor implements HttpRequestExecutor<AccessTokenRevokeRequest, Void, OkHttpClient> {

    @Override
    public @NotNull Optional<Void> execute(@NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull AccessTokenRevokeRequest requestInst) throws HttpRequestExecutionException {
        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("clientId", requestInst.clientId());
        requestJson.addProperty("clientSecret", requestInst.clientSecret());
        requestJson.addProperty("token", requestInst.token());
        requestJson.addProperty("tokenTypeHint", requestInst.tokenTypeHint().getAsString());

        RequestBody body = RequestBody.create(Constants.MEDIA_TYPE_JSON, requestJson.toString());

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/auth/v1/token/revoke")
                .post(body)
                .build();

        try (Response ignored = client.getNativeHttpClient().newCall(request).execute()) {
            return Optional.empty();
        } catch (IOException e) {
            throw new HttpRequestExecutionException("Failed to execute AccessTokenRevoke request", e);
        }
    }

}
