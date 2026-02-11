package kr.rtustudio.donation.bukkit.manager;

import com.google.gson.Gson;
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.component.PlatformStatusComponent;
import kr.rtustudio.donation.bukkit.entity.DonationPlayerEntity;
import kr.rtustudio.donation.bukkit.handler.DonationModule;
import kr.rtustudio.donation.bukkit.platform.DonationPlatform;
import kr.rtustudio.donation.bukkit.system.PlatformStatusSystem;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.platform.JSON;
import kr.rtustudio.framework.bukkit.api.storage.Storage;

import java.util.UUID;

/**
 * 후원 플레이어 관리자
 * <p>
 * 플레이어 데이터의 저장소 연동을 담당합니다.
 */
public class DonationPlayerManager {

    private static final Gson GSON = new Gson();

    private final BukkitDonationAPI plugin;
    private final DonationModule module;
    private final PlatformStatusSystem statusSystem;

    public DonationPlayerManager(BukkitDonationAPI plugin) {
        this.plugin = plugin;
        this.module = new DonationModule();
        this.statusSystem = new PlatformStatusSystem();
    }

    public void load(UUID uuid) {
        Storage storage = plugin.getStorage();
        storage.get("PlayerStatus", JSON.of("uuid", uuid.toString())).thenAccept(result -> {
            DonationPlayerEntity entity = result.isEmpty()
                    ? new DonationPlayerEntity(uuid)
                    : new DonationPlayerEntity(uuid, GSON.fromJson(result.getFirst(), PlatformStatusComponent.class));
            module.addPlayer(uuid, entity);
        });
    }

    public void unload(UUID uuid) {
        DonationPlayerEntity entity = module.getPlayer(uuid);
        if (entity == null) return;
        save(entity);
        module.removePlayer(uuid);
    }

    public void markConnected(UUID uuid, Services service) {
        statusSystem.markConnected(uuid, service);
        save(module.getPlayer(uuid));
    }

    public void markDisconnected(UUID uuid, Services service) {
        statusSystem.markDisconnected(uuid, service);
        save(module.getPlayer(uuid));
    }

    public void markDonationReceived(UUID uuid, Services service) {
        statusSystem.markDonationReceived(uuid, service);
        save(module.getPlayer(uuid));
    }

    public boolean isActive(UUID uuid, Services service) {
        return statusSystem.isActive(uuid, service);
    }

    public boolean isActive(UUID uuid, Services service, Platform platform) {
        DonationPlatform<?> donationPlatform = plugin.getPlatformRegistry().getPlatform(service);
        return donationPlatform != null && donationPlatform.isActive(uuid, platform);
    }

    public void resetDonationStatus(UUID uuid, Services service) {
        statusSystem.resetDonationStatus(uuid, service);
        save(module.getPlayer(uuid));
    }

    private void save(DonationPlayerEntity entity) {
        Storage storage = plugin.getStorage();
        UUID uuid = entity.getUuid();
        PlatformStatusComponent component = entity.getPlatformStatus();

        storage.get("PlayerStatus", JSON.of("uuid", uuid.toString())).thenAccept(result -> {
            if (result == null || result.isEmpty()) {
                storage.add("PlayerStatus", GSON.toJsonTree(component).getAsJsonObject());
            } else {
                storage.set("PlayerStatus", JSON.of("uuid", uuid.toString()).get(),
                        GSON.toJsonTree(component).getAsJsonObject());
            }
        });
    }
}
