package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.chzzk.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.net.data.AccessTokenRefreshRequest;
import kr.rtustudio.donation.service.chzzk.net.data.AccessTokenRefreshResponse;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.utils.Constants;
import kr.rtustudio.donation.service.chzzk.utils.HttpResponseParser;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

@Slf4j(topic = "DonationAPI/CHZZK")
public class AccessTokenRefreshExecutor implements HttpRequestExecutor<AccessTokenRefreshRequest, AccessTokenRefreshResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<AccessTokenRefreshResponse> execute(
            @NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull AccessTokenRefreshRequest requestInst) {

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("grantType", requestInst.grantType());
        requestJson.addProperty("refreshToken", requestInst.refreshToken());
        requestJson.addProperty("clientId", requestInst.clientId());
        requestJson.addProperty("clientSecret", requestInst.clientSecret());

        RequestBody body = RequestBody.create(Constants.MEDIA_TYPE_JSON, requestJson.toString());

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/auth/v1/token")
                .post(body)
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String responseBody = response.body() != null ? response.body().string() : "null";
                log.warn("Token refresh HTTP failed: status={}, body={}", response.code(), responseBody);
                return Optional.empty();
            }
            return HttpResponseParser.parse(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            log.error("Token refresh exception", e);
            throw new RuntimeException(e);
        }
    }

}
