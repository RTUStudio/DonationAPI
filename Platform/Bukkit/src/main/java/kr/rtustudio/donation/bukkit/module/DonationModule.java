package kr.rtustudio.donation.bukkit.module;

import kr.rtustudio.donation.bukkit.entity.DonationEntity;
import kr.rtustudio.donation.bukkit.system.PlatformStatusSystem;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 후원 모듈
 * <p>
 * 플레이어 엔티티를 관리하는 ECS Module입니다.
 * entityMap을 소유하고 엔티티의 생성/삭제/조회를 담당합니다.
 */
public class DonationModule {

    private final Map<UUID, DonationEntity> entityMap = new ConcurrentHashMap<>();

    @Getter
    private final PlatformStatusSystem statusSystem = new PlatformStatusSystem();

    public DonationEntity addPlayer(UUID uuid, DonationEntity entity) {
        entityMap.put(uuid, entity);
        return entity;
    }

    public void removePlayer(UUID uuid) {
        entityMap.remove(uuid);
    }

    public DonationEntity getPlayer(UUID uuid) {
        return entityMap.get(uuid);
    }
}
