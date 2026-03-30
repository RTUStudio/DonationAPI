package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import com.google.gson.reflect.TypeToken;

import kr.rtustudio.donation.service.chzzk.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.net.data.SessionSearchRequest;
import kr.rtustudio.donation.service.chzzk.net.data.SessionSearchResponse;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.utils.Constants;
import kr.rtustudio.donation.service.chzzk.utils.HttpResponseParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class SessionSearchExecutor implements HttpRequestExecutor<SessionSearchRequest, SessionSearchResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<SessionSearchResponse> execute(@NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull SessionSearchRequest requestInst) {
        HttpUrl url = HttpUrl.get(Constants.OPENAPI_URL + "/open/v1/sessions")
                .newBuilder()
                .addQueryParameter("size", Integer.toString(requestInst.size()))
                .addQueryParameter("page", requestInst.page())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + requestInst.accessToken())
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return HttpResponseParser.parse(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            throw new RuntimeException("Failed to execute SessionSearch request", e);
        }
    }

}
