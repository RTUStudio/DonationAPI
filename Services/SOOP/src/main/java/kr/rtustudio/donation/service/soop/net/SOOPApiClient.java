package kr.rtustudio.donation.service.soop.net;

import kr.rtustudio.donation.service.soop.net.data.*;
import kr.rtustudio.donation.service.soop.net.http.client.SoopHttpClient;
import kr.rtustudio.donation.service.soop.net.http.executor.okhttp.AccessTokenGrantExecutor;
import kr.rtustudio.donation.service.soop.net.http.executor.okhttp.AccessTokenRefreshExecutor;
import kr.rtustudio.donation.service.soop.net.http.executor.okhttp.ChatInfoExecutor;
import kr.rtustudio.donation.service.soop.net.http.executor.okhttp.StationInfoExecutor;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SoopApiClient {

    private final @NotNull SoopHttpClient<OkHttpClient> httpClient;
    private final @NotNull String clientId;
    private final @NotNull String clientSecret;

    private final AccessTokenGrantExecutor grantExecutor = new AccessTokenGrantExecutor();
    private final AccessTokenRefreshExecutor refreshExecutor = new AccessTokenRefreshExecutor();
    private final StationInfoExecutor stationInfoExecutor = new StationInfoExecutor();
    private final ChatInfoExecutor chatInfoExecutor = new ChatInfoExecutor();

    public SoopApiClient(@NotNull String clientId, @NotNull String clientSecret) {
        this.httpClient = SoopHttpClient.okhttp();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public @NotNull Optional<AccessTokenGrantResponse> grantToken(@NotNull String code) {
        return grantExecutor.execute(httpClient, new AccessTokenGrantRequest(clientId, clientSecret, code));
    }

    public @NotNull Optional<AccessTokenRefreshResponse> refreshToken(@NotNull String refreshToken) {
        return refreshExecutor.execute(httpClient, new AccessTokenRefreshRequest(clientId, clientSecret, refreshToken));
    }

    public @NotNull Optional<StationInfoResponse> getStationInfo(@NotNull String accessToken) {
        return stationInfoExecutor.execute(httpClient, new StationInfoRequest(accessToken));
    }

    public @NotNull Optional<ChatInfoResponse> getChatInfo(@NotNull String accessToken) {
        return chatInfoExecutor.execute(httpClient, new ChatInfoRequest(accessToken));
    }

}
