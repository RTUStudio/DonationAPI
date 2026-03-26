package kr.rtustudio.donation.service.soop;

import kr.rtustudio.donation.service.AbstractService;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.soop.configuration.SOOPConfig;
import kr.rtustudio.donation.service.soop.data.SOOPPlayer;
import kr.rtustudio.donation.service.soop.data.SOOPToken;
import kr.rtustudio.donation.service.soop.net.AuthResult;
import kr.rtustudio.donation.service.soop.net.SOOPApiClient;
import kr.rtustudio.donation.service.soop.net.SOOPAuthServerHandler;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j(topic = "DonationAPI/SOOP")
public class SOOPService extends AbstractService<SOOPPlayer> implements kr.rtustudio.donation.service.Disconnectable {

    @Getter
    private final SOOPConfig config;

    @Getter
    private final Map<String, SOOPToken> tokenStore = new ConcurrentHashMap<>();

    private SOOPSubscriber subscriber;
    private SOOPAuthServer authServer;
    @Getter
    private SOOPApiClient apiClient;

    public SOOPService(SOOPConfig config, ServiceHandler<SOOPPlayer> handler) {
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

        this.subscriber = new SOOPSubscriber(this);
        this.apiClient = new SOOPApiClient(config.getClientId(), config.getClientSecret());
        this.authServer = new SOOPAuthServer(
                config.getClientId(),
                config.getClientSecret(),
                config.getBaseUri(),
                config.getHost(),
                config.getPort(),
                new SOOPAuthServerHandler() {
                    @Override
                    public boolean onSuccess(@NotNull AuthResult result) {
                        return handleAuthSuccess(result);
                    }
                }
        );
        authServer.start();
        log.info("SOOP service started");
    }

    private boolean handleAuthSuccess(@NotNull AuthResult result) {
        var tokenResponse = apiClient.grantToken(result.code());
        if (tokenResponse.isEmpty()) {
            log.error("Token grant failed for user: {}", result.user());
            return false;
        }

        SOOPToken token = new SOOPToken(tokenResponse.get().accessToken(), tokenResponse.get().refreshToken());
        var stationInfo = apiClient.getStationInfo(token.accessToken());
        if (stationInfo.isEmpty()) {
            log.error("Failed to get station info after auth");
            return false;
        }

        String userId = stationInfo.get().stationName() != null ? stationInfo.get().stationName() : "unknown";
        tokenStore.put(userId, token);
        boolean connected = subscriber.onUserRegistered(userId, result.user());
        if (connected) {
            log.info("Registered SOOP subscriber for bjId: {} (UUID: {})", stationInfo.get().stationName(), result.user());
        }
        return connected;
    }

    public boolean reconnect(@NotNull UUID uuid, @NotNull SOOPToken token) {
        if (subscriber == null) {
            log.warn("Cannot reconnect: service not started");
            return false;
        }

        return apiClient.refreshToken(token.refreshToken()).map(tokenResponse -> {
            SOOPToken newToken = new SOOPToken(tokenResponse.accessToken(), tokenResponse.refreshToken());

            return apiClient.getStationInfo(newToken.accessToken()).map(stationInfo -> {
                String userId = stationInfo.stationName() != null ? stationInfo.stationName() : "unknown";
                tokenStore.put(userId, newToken);
                subscriber.onUserRegistered(userId, uuid.toString());
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
        if (subscriber != null) {
            subscriber.closeAll();
            subscriber = null;
        }
        if (authServer != null) {
            authServer.stop();
            authServer = null;
        }
        tokenStore.clear();
    }
}
