package kr.rtustudio.donation.service.soop;

import kr.rtustudio.donation.service.AbstractService;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.soop.configuration.SoopConfig;
import kr.rtustudio.donation.service.soop.data.SoopPlayer;
import kr.rtustudio.donation.service.soop.data.SoopToken;
import kr.rtustudio.donation.service.soop.net.AuthResult;
import kr.rtustudio.donation.service.soop.net.SoopApiClient;
import kr.rtustudio.donation.service.soop.net.SoopAuthServerHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "DonationAPI/SOOP")
public class SoopService extends AbstractService<SoopPlayer> implements kr.rtustudio.donation.service.Disconnectable {

    private static final long LIVE_POLL_INTERVAL_SECONDS = 30;

    @Getter
    private final SoopConfig config;

    @Getter
    private final Map<String, SoopToken> tokenStore = new ConcurrentHashMap<>();

    @Getter
    private final Map<String, String> stationNameStore = new ConcurrentHashMap<>();

    private SoopSubscriber subscriber;
    private SoopAuthServer authServer;
    @Getter
    private SoopApiClient apiClient;
    @Getter
    @Nullable
    private SoopLiveMonitor liveMonitor;

    public SoopService(SoopConfig config, ServiceHandler<SoopPlayer> handler) {
        super(handler);
        this.config = config;
    }

    @Override
    public Services getType() {
        return Services.SOOP;
    }

    @Override
    public void start() {
        if (!config.isEnabled()) return;

        this.subscriber = new SoopSubscriber(this);
        this.apiClient = new SoopApiClient(config.getClientId(), config.getClientSecret());
        this.liveMonitor = new SoopLiveMonitor(this, subscriber);
        this.authServer = new SoopAuthServer(
                config.getClientId(),
                config.getClientSecret(),
                config.getBaseUri(),
                config.getHost(),
                config.getPort(),
                new SoopAuthServerHandler() {
                    @Override
                    public boolean onSuccess(@NotNull AuthResult result) {
                        return handleAuthSuccess(result);
                    }
                }
        );
        authServer.start();
        liveMonitor.start(LIVE_POLL_INTERVAL_SECONDS);
        log.debug("SOOP service started");
    }

    private boolean handleAuthSuccess(@NotNull AuthResult result) {
        var tokenResponse = apiClient.grantToken(result.code());
        if (tokenResponse.isEmpty()) {
            log.error("Token grant failed for user: {}", result.user());
            return false;
        }

        SoopToken token = new SoopToken(tokenResponse.get().accessToken(), tokenResponse.get().refreshToken());
        var stationInfo = apiClient.getStationInfo(token.accessToken());
        if (stationInfo.isEmpty()) {
            log.error("Failed to get station info after auth");
            return false;
        }

        String bjId = extractBjId(stationInfo.get().profileImage());
        if (bjId == null) {
            log.error("Failed to extract bjId from profile image: {}", stationInfo.get().profileImage());
            return false;
        }
        String stationName = stationInfo.get().stationName() != null ? stationInfo.get().stationName() : bjId;
        tokenStore.put(bjId, token);
        stationNameStore.put(bjId, stationName);
        return subscriber.onUserRegistered(bjId, result.user(), stationName);
    }

    public boolean reconnect(@NotNull UUID uuid, @NotNull SoopToken token) {
        if (subscriber == null) {
            log.warn("Cannot reconnect: service not started");
            return false;
        }

        return apiClient.refreshToken(token.refreshToken()).map(tokenResponse -> {
            SoopToken newToken = new SoopToken(tokenResponse.accessToken(), tokenResponse.refreshToken());

            return apiClient.getStationInfo(newToken.accessToken()).map(stationInfo -> {
                String bjId = extractBjId(stationInfo.profileImage());
                if (bjId == null) {
                    log.error("Failed to extract bjId from profile image: {}", stationInfo.profileImage());
                    return false;
                }
                String stationName = stationInfo.stationName() != null ? stationInfo.stationName() : bjId;
                tokenStore.put(bjId, newToken);
                stationNameStore.put(bjId, stationName);
                subscriber.onUserRegistered(bjId, uuid.toString(), stationName);
                return true;
            }).orElse(false);
        }).orElseGet(() -> {
            log.error("Failed to refresh token for player {}, token may be expired", uuid);
            return false;
        });
    }

    @Override
    public void disconnect(@NotNull UUID uuid) {
        if (subscriber != null) {
            subscriber.disconnect(uuid);
        }
    }

    @Override
    public void close() {
        if (liveMonitor != null) {
            liveMonitor.closeAll();
            liveMonitor = null;
        }
        if (subscriber != null) {
            subscriber.closeAll();
            subscriber = null;
        }
        if (authServer != null) {
            authServer.stop();
            authServer = null;
        }
        tokenStore.clear();
        stationNameStore.clear();
    }

    /**
     * 프로필 이미지 URL에서 실제 bjId를 추출합니다.
     * URL 형식: https://profile.img.sooplive.com/LOGO/xx/bjId/bjId.jpg
     */
    private static @Nullable String extractBjId(@Nullable String profileImageUrl) {
        if (profileImageUrl == null || profileImageUrl.isEmpty()) return null;
        int lastSlash = profileImageUrl.lastIndexOf('/');
        if (lastSlash < 0) return null;
        String filename = profileImageUrl.substring(lastSlash + 1);
        int dot = filename.lastIndexOf('.');
        return dot > 0 ? filename.substring(0, dot) : filename;
    }
}
