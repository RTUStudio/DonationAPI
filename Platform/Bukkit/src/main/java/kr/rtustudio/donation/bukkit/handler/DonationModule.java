package kr.rtustudio.donation.bukkit.handler;

import kr.rtustudio.donation.bukkit.entity.DonationPlayerEntity;
import kr.rtustudio.donation.bukkit.system.PlatformStatusSystem;
import kr.rtustudio.donation.service.Services;
import lombok.Getter;

import java.util.UUID;

/**
 * 후원 모듈
 * <p>
 * 플레이어 엔티티와 플랫폼 상태를 관리합니다.
 */
public class DonationModule {

    @Getter
    private final PlatformStatusSystem statusSystem = new PlatformStatusSystem();

    public DonationPlayerEntity addPlayer(UUID uuid, DonationPlayerEntity entity) {
        return statusSystem.addPlayer(uuid, entity);
    }

    public void removePlayer(UUID uuid) {
        statusSystem.removePlayer(uuid);
    }

    public DonationPlayerEntity getPlayer(UUID uuid) {
        return statusSystem.getPlayer(uuid);
    }
}
