package kr.rtustudio.donation.bukkit.manager;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.component.PlatformStatusComponent;
import kr.rtustudio.donation.bukkit.entity.DonationEntity;
import kr.rtustudio.donation.bukkit.module.DonationModule;
import kr.rtustudio.donation.bukkit.platform.DonationPlatform;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.storage.JSON;
import kr.rtustudio.storage.Storage;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 후원 플레이어 관리자
 * <p>
 * 플레이어 데이터의 저장소 연동을 담당합니다.
 */
public class DonationManager {

    private static final Gson GSON = new Gson();

    private final BukkitDonationAPI plugin;
    private final DonationModule module;
    private final Set<UUID> knownUsers = ConcurrentHashMap.newKeySet();

    public DonationManager(BukkitDonationAPI plugin) {
        this.plugin = plugin;
        this.module = new DonationModule();
    }

    public CompletableFuture<Void> load(UUID uuid) {
        Storage storage = plugin.getStorage("User");
        return storage.get(JSON.of("uuid", uuid.toString())).thenAccept(result -> {
            DonationEntity entity;
            if (result.isEmpty()) {
                entity = new DonationEntity(uuid);
            } else {
                entity = new DonationEntity(uuid, GSON.fromJson(result.getFirst(), PlatformStatusComponent.class));
                knownUsers.add(uuid);
            }
            module.addPlayer(uuid, entity);
        });
    }

    public void unload(UUID uuid) {
        DonationEntity entity = module.getPlayer(uuid);
        if (entity == null) return;
        save(entity);
        module.removePlayer(uuid);
    }

    public void markConnected(UUID uuid, Services service) {
        DonationEntity entity = module.getPlayer(uuid);
        if (entity == null) return;
        module.getStatusSystem().markConnected(entity, service);
        save(entity);
    }

    public void markDisconnected(UUID uuid, Services service) {
        DonationEntity entity = module.getPlayer(uuid);
        if (entity == null) return;
        module.getStatusSystem().markDisconnected(entity, service);
        save(entity);
    }

    public void markDonationReceived(UUID uuid, Services service) {
        DonationEntity entity = module.getPlayer(uuid);
        if (entity == null) return;
        module.getStatusSystem().markDonationReceived(entity, service);
        save(entity);
    }

    public boolean isActive(UUID uuid, Services service) {
        DonationEntity entity = module.getPlayer(uuid);
        return entity != null && module.getStatusSystem().isActive(entity, service);
    }

    public boolean isActive(UUID uuid, Services service, Platform platform) {
        DonationPlatform<?> donationPlatform = plugin.getPlatformRegistry().getPlatform(service);
        return donationPlatform != null && donationPlatform.isActive(uuid, platform);
    }

    public void resetDonationStatus(UUID uuid, Services service) {
        DonationEntity entity = module.getPlayer(uuid);
        if (entity == null) return;
        module.getStatusSystem().resetDonationStatus(entity, service);
        save(entity);
    }

    private void save(DonationEntity entity) {
        Storage storage = plugin.getStorage("User");
        UUID uuid = entity.getUuid();
        PlatformStatusComponent component = entity.getPlatformStatus();

        JsonObject data = GSON.toJsonTree(component).getAsJsonObject();
        data.addProperty("uuid", uuid.toString());

        JSON query = JSON.of("uuid", uuid.toString());
        if (knownUsers.contains(uuid)) {
            storage.set(query.get(), data);
        } else {
            knownUsers.add(uuid);
            storage.add(data);
        }
    }
}
