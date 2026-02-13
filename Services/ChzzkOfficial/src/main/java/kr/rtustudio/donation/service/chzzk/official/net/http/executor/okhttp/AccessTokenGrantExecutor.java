package kr.rtustudio.donation.service.chzzk.official.net.http.executor.okhttp;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.chzzk.official.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.official.net.data.AccessTokenGrantRequest;
import kr.rtustudio.donation.service.chzzk.official.net.data.AccessTokenGrantResponse;
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

public class AccessTokenGrantExecutor implements HttpRequestExecutor<AccessTokenGrantRequest, AccessTokenGrantResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<AccessTokenGrantResponse> execute(
            @NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull AccessTokenGrantRequest requestInst) {

        JsonObject requestJson = new JsonObject();
        requestJson.addProperty("grantType", requestInst.grantType());
        requestJson.addProperty("clientId", requestInst.clientId());
        requestJson.addProperty("clientSecret", requestInst.clientSecret());
        requestJson.addProperty("code", requestInst.code());
        requestJson.addProperty("state", requestInst.state());

        RequestBody body = RequestBody.create(Constants.MEDIA_TYPE_JSON, requestJson.toString());

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/auth/v1/token")
                .post(body)
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return HttpResponseParser.parse(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            throw new RuntimeException(e);
        }
    }

}
