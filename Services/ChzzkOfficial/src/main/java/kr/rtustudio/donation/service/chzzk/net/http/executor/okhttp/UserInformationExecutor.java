package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.chzzk.exception.HttpRequestExecutionException;
import kr.rtustudio.donation.service.chzzk.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.net.data.UserInformationRequest;
import kr.rtustudio.donation.service.chzzk.net.data.UserInformationResponse;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.utils.Constants;
import kr.rtustudio.donation.service.chzzk.utils.HttpResponseParser;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class UserInformationExecutor implements HttpRequestExecutor<UserInformationRequest, UserInformationResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<UserInformationResponse> execute(
            @NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull UserInformationRequest requestInst) throws HttpRequestExecutionException {

        Request request = new Request.Builder()
                .url(Constants.OPENAPI_URL + "/open/v1/users/me")
                .get()
                .addHeader("Authorization", "Bearer " + requestInst.accessToken())
                .addHeader("Content-Type", "application/json")
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return HttpResponseParser.parse(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            throw new HttpRequestExecutionException("Failed to execute UserInformation request", e);
        }
    }

}
