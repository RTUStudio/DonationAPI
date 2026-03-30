package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import com.google.gson.reflect.TypeToken;

import kr.rtustudio.donation.service.chzzk.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.net.data.RestrictionSearchRequest;
import kr.rtustudio.donation.service.chzzk.net.data.RestrictionSearchResponse;
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

public class RestrictionSearchExecutor implements HttpRequestExecutor<RestrictionSearchRequest, RestrictionSearchResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<RestrictionSearchResponse> execute(@NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull RestrictionSearchRequest requestInst) {
        HttpUrl url = HttpUrl.get(Constants.OPENAPI_URL + "/open/v1/restrict-channels")
                .newBuilder()
                .addQueryParameter("size", Integer.toString(requestInst.size()))
                .addQueryParameter("next", requestInst.next())
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
            throw new RuntimeException("Failed to execute RestrictionSearch request", e);
        }
    }

}
