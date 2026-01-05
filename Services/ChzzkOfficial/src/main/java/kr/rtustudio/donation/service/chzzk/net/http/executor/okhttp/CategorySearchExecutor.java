package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.chzzk.exception.HttpRequestExecutionException;
import kr.rtustudio.donation.service.chzzk.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.net.data.CategorySearchRequest;
import kr.rtustudio.donation.service.chzzk.net.data.CategorySearchResponse;
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

public class CategorySearchExecutor implements HttpRequestExecutor<CategorySearchRequest, CategorySearchResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<CategorySearchResponse> execute(
            @NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull CategorySearchRequest requestInst) throws HttpRequestExecutionException {

        HttpUrl url = HttpUrl.get(Constants.OPENAPI_URL + "/open/v1/categories/search")
                .newBuilder()
                .addQueryParameter("query", requestInst.query())
                .addQueryParameter("size", Integer.toString(requestInst.size()))
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
            throw new HttpRequestExecutionException("Failed to execute CategorySearch request", e);
        }
    }

}
