package kr.rtustudio.donation.bukkit.system;

import kr.rtustudio.donation.bukkit.entity.DonationEntity;
import kr.rtustudio.donation.service.Services;

/**
 * 플랫폼 상태 시스템
 * <p>
 * 플레이어 엔티티의 플랫폼 상태를 처리하는 ECS System입니다.
 * 엔티티를 매개변수로 받아 상태를 업데이트합니다.
 */
public class PlatformStatusSystem {

    public void markConnected(DonationEntity entity, Services service) {
        entity.getPlatformStatus().markConnected(service);
    }

    public void markDisconnected(DonationEntity entity, Services service) {
        entity.getPlatformStatus().markDisconnected(service);
    }

    public void markDonationReceived(DonationEntity entity, Services service) {
        entity.getPlatformStatus().markDonationReceived(service);
    }

    public boolean isActive(DonationEntity entity, Services service) {
        return entity != null && entity.getPlatformStatus().isActive(service);
    }

    public void resetDonationStatus(DonationEntity entity, Services service) {
        entity.getPlatformStatus().resetDonationStatus(service);
    }
}
