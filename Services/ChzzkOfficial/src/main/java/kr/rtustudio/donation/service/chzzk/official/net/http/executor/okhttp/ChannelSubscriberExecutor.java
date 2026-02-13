package kr.rtustudio.donation.service.chzzk.official.net.http.executor.okhttp;

import com.google.gson.reflect.TypeToken;

import kr.rtustudio.donation.service.chzzk.official.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.official.net.data.ChannelSubscriberRequest;
import kr.rtustudio.donation.service.chzzk.official.net.data.ChannelSubscriberResponse;
import kr.rtustudio.donation.service.chzzk.official.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.official.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.official.utils.Constants;
import kr.rtustudio.donation.service.chzzk.official.utils.HttpResponseParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class ChannelSubscriberExecutor implements HttpRequestExecutor<ChannelSubscriberRequest, ChannelSubscriberResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<ChannelSubscriberResponse> execute(@NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull ChannelSubscriberRequest requestInst) {
        HttpUrl url = HttpUrl.get(Constants.OPENAPI_URL + "/open/v1/channels/subscribers")
                .newBuilder()
                .addQueryParameter("page", Integer.toString(requestInst.page()))
                .addQueryParameter("size", Integer.toString(requestInst.size()))
                .addQueryParameter("sort", requestInst.sort())
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
            throw new RuntimeException("Failed to execute ChannelSubscriber request", e);
        }
    }

}
