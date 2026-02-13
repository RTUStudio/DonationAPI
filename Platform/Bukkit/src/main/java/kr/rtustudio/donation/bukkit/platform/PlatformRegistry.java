package kr.rtustudio.donation.bukkit.platform;

import kr.rtustudio.donation.service.Services;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 플랫폼 레지스트리
 * <p>
 * 등록된 모든 후원 플랫폼을 관리합니다.
 */
public class PlatformRegistry {

    private final Map<Services, DonationPlatform<?>> platforms = new ConcurrentHashMap<>();

    /**
     * 플랫폼을 레지스트리에 등록합니다.
     *
     * @param platform 등록할 플랫폼
     */
    public void register(DonationPlatform<?> platform) {
        platforms.put(platform.getService(), platform);
        platform.initialize();
    }

    /**
     * 서비스에 해당하는 플랫폼을 가져옵니다.
     *
     * @param service 서비스 타입
     * @return 플랫폼 인스턴스
     */
    public DonationPlatform<?> getPlatform(Services service) {
        return platforms.get(service);
    }

    /**
     * 활성화된 모든 플랫폼을 가져옵니다.
     *
     * @return 활성화된 플랫폼 컬렉션
     */
    public Collection<DonationPlatform<?>> getEnabledPlatforms() {
        return platforms.values().stream()
                .filter(DonationPlatform::isEnabled)
                .toList();
    }

    /**
     * 모든 플랫폼을 종료하고 레지스트리를 정리합니다.
     */
    public void shutdown() {
        platforms.values().forEach(DonationPlatform::shutdown);
        platforms.clear();
    }
}
