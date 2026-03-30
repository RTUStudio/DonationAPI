package kr.rtustudio.donation.bukkit.manager;

import kr.rtustudio.donation.bukkit.platform.AbstractDonationPlatform;
import kr.rtustudio.donation.bukkit.platform.DonationPlatform;
import kr.rtustudio.donation.bukkit.platform.PlatformRegistry;
import kr.rtustudio.donation.service.data.UserData;
import kr.rtustudio.donation.service.Services;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * 플랫폼 연결 관리자
 * <p>
 * 플레이어의 플랫폼 연결 및 해제를 관리합니다.
 * 플랫폼 레지스트리를 통해 등록된 플랫폼에 접근합니다.
 */
@RequiredArgsConstructor
public class PlatformConnectionManager {

    private final PlatformRegistry registry;

    /**
     * 플레이어를 플랫폼에 연결합니다.
     *
     * @param uuid    플레이어 UUID
     * @param service 서비스 타입
     * @param data    연결 데이터 객체
     * @param <T>     데이터 타입
     * @return 연결 성공 여부
     */
    @SuppressWarnings("unchecked")
    public <T extends UserData> boolean connect(UUID uuid, Services service, T data) {
        DonationPlatform<T> platform = (DonationPlatform<T>) registry.getPlatform(service);
        return platform != null && platform.connect(uuid, data);
    }

    /**
     * 플레이어의 플랫폼 연결을 해제합니다.
     *
     * @param uuid    플레이어 UUID
     * @param service 서비스 타입
     */
    public void disconnect(UUID uuid, Services service) {
        DonationPlatform<?> platform = registry.getPlatform(service);
        if (platform != null) platform.disconnect(uuid);
    }

    /**
     * 플레이어의 모든 플랫폼 연결 정보를 불러옵니다.
     *
     * @param uuid 플레이어 UUID
     */
    public void loadAll(UUID uuid) {
        registry.getEnabledPlatforms().forEach(platform -> platform.load(uuid));
    }

    /**
     * 플레이어가 플랫폼에 연결되어 있는지 확인합니다.
     *
     * @param uuid    플레이어 UUID
     * @param service 서비스 타입
     * @return 연결 여부
     */
    public boolean isConnected(UUID uuid, Services service) {
        DonationPlatform<?> platform = registry.getPlatform(service);
        return platform instanceof AbstractDonationPlatform<?> abstractPlatform && abstractPlatform.isConnected(uuid);
    }
}
