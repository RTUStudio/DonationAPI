package kr.rtustudio.donation.service.soop.net.http.executor.okhttp;

import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.soop.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.soop.net.data.StationInfoRequest;
import kr.rtustudio.donation.service.soop.net.data.StationInfoResponse;
import kr.rtustudio.donation.service.soop.net.http.client.SOOPHttpClient;
import kr.rtustudio.donation.service.soop.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.soop.utils.Constants;
import kr.rtustudio.donation.service.soop.utils.HttpResponseParser;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class StationInfoExecutor implements HttpRequestExecutor<StationInfoRequest, StationInfoResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<StationInfoResponse> execute(
            @NotNull SOOPHttpClient<OkHttpClient> client, @NotNull StationInfoRequest requestInst) {

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/user/stationinfo")
                .post(new FormBody.Builder()
                        .add("access_token", requestInst.accessToken())
                        .build())
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return HttpResponseParser.parse(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            throw new RuntimeException("Failed to execute StationInfo request", e);
        }
    }

}
