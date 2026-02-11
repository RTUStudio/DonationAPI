package kr.rtustudio.donation.bukkit.system;

import kr.rtustudio.donation.bukkit.component.PlatformStatusComponent;
import kr.rtustudio.donation.bukkit.entity.DonationPlayerEntity;
import kr.rtustudio.donation.service.Services;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 플랫폼 상태 시스템
 * <p>
 * 플레이어 엔티티의 플랫폼 상태를 관리하는 ECS System입니다.
 */
public class PlatformStatusSystem {

    private final Map<UUID, DonationPlayerEntity> entityMap = new ConcurrentHashMap<>();

    public DonationPlayerEntity addPlayer(UUID uuid, DonationPlayerEntity entity) {
        entityMap.put(uuid, entity);
        return entity;
    }

    public void removePlayer(UUID uuid) {
        entityMap.remove(uuid);
    }

    public DonationPlayerEntity getPlayer(UUID uuid) {
        return entityMap.get(uuid);
    }

    public DonationPlayerEntity getOrCreatePlayer(UUID uuid) {
        return entityMap.computeIfAbsent(uuid, DonationPlayerEntity::new);
    }

    public void markConnected(UUID uuid, Services service) {
        getOrCreatePlayer(uuid).getPlatformStatus().markConnected(service);
    }

    public void markDisconnected(UUID uuid, Services service) {
        getOrCreatePlayer(uuid).getPlatformStatus().markDisconnected(service);
    }

    public void markDonationReceived(UUID uuid, Services service) {
        getOrCreatePlayer(uuid).getPlatformStatus().markDonationReceived(service);
    }

    public boolean isActive(UUID uuid, Services service) {
        DonationPlayerEntity entity = getPlayer(uuid);
        return entity != null && entity.getPlatformStatus().isActive(service);
    }

    public void resetDonationStatus(UUID uuid, Services service) {
        getOrCreatePlayer(uuid).getPlatformStatus().resetDonationStatus(service);
    }
}
