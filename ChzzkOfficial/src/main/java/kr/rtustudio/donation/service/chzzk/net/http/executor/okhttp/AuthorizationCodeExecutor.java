package kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp;

import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.chzzk.exception.HttpRequestExecutionException;
import kr.rtustudio.donation.service.chzzk.exception.HttpResponseParseException;
import kr.rtustudio.donation.service.chzzk.net.data.AuthorizationCodeRequest;
import kr.rtustudio.donation.service.chzzk.net.data.AuthorizationCodeResponse;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.utils.HttpResponseParser;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class AuthorizationCodeExecutor implements HttpRequestExecutor<AuthorizationCodeRequest, AuthorizationCodeResponse, OkHttpClient> {

    @Override
    public @NotNull Optional<AuthorizationCodeResponse> execute(
            @NotNull ChzzkHttpClient<OkHttpClient> client, @NotNull AuthorizationCodeRequest requestInst) throws HttpRequestExecutionException {

        HttpUrl url = HttpUrl.get("https://chzzk.naver.com/account-interlock").newBuilder()
                .addQueryParameter("clientId", requestInst.clientId())
                .addQueryParameter("redirectUri", requestInst.redirectUri())
                .addQueryParameter("state", requestInst.state())
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        try (Response response = client.getNativeHttpClient().newCall(request).execute()) {
            return HttpResponseParser.parse(response, new TypeToken<>() {
            });
        } catch (IOException | HttpResponseParseException e) {
            throw new HttpRequestExecutionException("Failed to execute AuthorizationCode request", e);
        }
    }

}
