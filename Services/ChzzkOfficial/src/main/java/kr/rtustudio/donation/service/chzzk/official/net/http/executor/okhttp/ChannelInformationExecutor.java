package kr.rtustudio.donation.service.chzzk.official.net.http.executor.okhttp;

import com.google.gson.reflect.TypeToken;

import kr.rtustudio.donation.service.chzzk.official.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.official.net.data.ChannelInformationRequest;
import kr.rtustudio.donation.service.chzzk.official.net.data.ChannelInformationResponse;
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

public class ChannelInformationExecutor implements HttpRequestExecutor<ChannelInformationRequest, ChannelInformationResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<ChannelInformationResponse> execute(
            @NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull ChannelInformationRequest requestInst) {

        HttpUrl.Builder builder = HttpUrl.get(Constants.OPENAPI_URL + "/open/v1/channels").newBuilder();
        requestInst.channelIds().forEach(id -> builder.addQueryParameter("channelIds", id));

        HttpUrl url = builder.build();

        Request request = new Request.Builder()
                .url(url)
                .addHeader("Client-Id", requestInst.clientId())
                .addHeader("Client-Secret", requestInst.clientSecret())
                .addHeader("Content-Type", "application/json")
                .get()
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return HttpResponseParser.parse(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            throw new RuntimeException("Failed to execute ChannelInformation request", e);
        }
    }

}
