package kr.rtustudio.donation.bukkit.manager;

import kr.rtustudio.donation.bukkit.platform.AbstractDonationPlatform;
import kr.rtustudio.donation.bukkit.platform.PlatformRegistry;
import kr.rtustudio.donation.common.data.LiveStatus;
import kr.rtustudio.donation.common.live.LiveStatusChecker;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.data.UserData;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 라이브 상태 캐시 매니저
 * <p>
 * 등록된 플레이어의 채널에 대해 주기적으로 라이브 상태를 폴링하여 캐싱합니다.
 */
@Slf4j(topic = "DonationAPI")
public class LiveStatusManager {

    private final Map<String, LiveStatus> cache = new ConcurrentHashMap<>();
    private final Map<Services, LiveStatusChecker> checkers = new ConcurrentHashMap<>();
    private final PlatformRegistry registry;
    private final ScheduledExecutorService scheduler;

    public LiveStatusManager(PlatformRegistry registry) {
        this.registry = registry;
        this.scheduler = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "LiveStatusPoller");
            t.setDaemon(true);
            return t;
        });
    }

    /**
     * 서비스의 라이브 체커를 등록하고 주기적 폴링을 시작합니다.
     *
     * @param service    서비스 타입
     * @param checker    라이브 체커 구현체
     * @param intervalMs 폴링 주기 (밀리초)
     */
    public void registerChecker(Services service, LiveStatusChecker checker, long intervalMs) {
        checkers.put(service, checker);
        scheduler.scheduleAtFixedRate(() -> pollService(service, checker), intervalMs, intervalMs, TimeUnit.MILLISECONDS);
        log.info("Registered live status checker for {} (interval: {}ms)", service.name(), intervalMs);
    }

    private void pollService(Services service, LiveStatusChecker checker) {
        try {
            var platform = registry.getPlatform(service);
            if (platform instanceof AbstractDonationPlatform<?> abstractPlatform) {
                for (Map.Entry<UUID, ? extends UserData> entry : getConnections(abstractPlatform).entrySet()) {
                    String channelId = entry.getValue().channelId();
                    if (channelId == null || channelId.isEmpty()) continue;

                    String cacheKey = service.name() + ":" + channelId;
                    checker.checkLive(channelId).thenAccept(status -> cache.put(cacheKey, status));
                }
            }
        } catch (Exception e) {
            log.warn("Error polling live status for {}: {}", service.name(), e.getMessage());
        }
    }

    private <T extends UserData> Map<UUID, T> getConnections(AbstractDonationPlatform<T> platform) {
        return platform.getConnections();
    }

    /**
     * 캐싱된 라이브 상태를 조회합니다.
     *
     * @param service   서비스 타입
     * @param channelId 채널 식별자
     * @return 캐싱된 라이브 상태 (없으면 null)
     */
    public LiveStatus getLiveStatus(Services service, String channelId) {
        return cache.get(service.name() + ":" + channelId);
    }

    /**
     * 스케줄러를 종료합니다.
     */
    public void shutdown() {
        scheduler.shutdownNow();
        cache.clear();
    }
}
