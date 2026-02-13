package kr.rtustudio.donation.bukkit.platform;

import com.google.gson.Gson;
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.DonationPlayerManager;
import kr.rtustudio.donation.service.data.UserData;
import kr.rtustudio.framework.bukkit.api.platform.JSON;
import kr.rtustudio.framework.bukkit.api.player.PlayerChat;
import kr.rtustudio.framework.bukkit.api.storage.Storage;
import lombok.Getter;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 후원 플랫폼 추상 클래스
 * <p>
 * {@link DonationPlatform} 인터페이스의 기본 구현을 제공합니다.
 * 연결 관리, 저장소 처리, 플레이어 알림 등의 공통 기능을 구현합니다.
 *
 * @param <T> 플랫폼별 연결 데이터 타입 (UserData를 구현해야 함)
 */
public abstract class AbstractDonationPlatform<T extends UserData> implements DonationPlatform<T> {

    @Getter
    protected final BukkitDonationAPI plugin;
    protected final DonationPlayerManager playerManager;
    protected final Storage storage;
    protected final PlayerChat chat;
    protected final Gson gson;

    protected final ConcurrentHashMap<UUID, T> connections = new ConcurrentHashMap<>();

    protected AbstractDonationPlatform(BukkitDonationAPI plugin, Gson serializer) {
        this.plugin = plugin;
        this.playerManager = plugin.getPlayerManager();
        this.storage = plugin.getStorage();
        this.chat = PlayerChat.of(plugin);
        this.gson = serializer;
    }

    @Override
    public boolean connect(UUID uuid, T data) {
        try {
            connections.put(uuid, data);
            save(uuid, data);
            playerManager.markConnected(uuid, getService());
            onRegister(uuid, data);
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to connect player " + uuid + " to " + getService() + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public void disconnect(UUID uuid) {
        connections.remove(uuid);
        delete(uuid);
        playerManager.markDisconnected(uuid, getService());
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
        connections.clear();
    }

    public T getConnection(UUID uuid) {
        return connections.get(uuid);
    }

    public boolean isConnected(UUID uuid) {
        return connections.containsKey(uuid);
    }

    @Override
    public boolean isActive(UUID uuid) {
        return isConnected(uuid) && playerManager.isActive(uuid, getService());
    }

    protected abstract Class<T> dataClass();

    @Override
    public void load(UUID uuid) {
        String storageName = getService().getStorage();
        storage.get(storageName, JSON.of("uuid", uuid.toString()))
                .thenAccept(result -> {
                    if (result.isEmpty()) {
                        playerManager.resetDonationStatus(uuid, getService());
                        return;
                    }
                    T data = gson.fromJson(result.getFirst(), dataClass());
                    if (onReconnect(uuid, data)) {
                        connections.put(uuid, data);
                        playerManager.markConnected(uuid, getService());
                    } else {
                        connections.remove(uuid);
                        playerManager.resetDonationStatus(uuid, getService());
                    }
                });
    }

    protected void onRegister(UUID uuid, T data) {
    }

    protected boolean onReconnect(UUID uuid, T data) {
        return true;
    }

    protected void save(UUID uuid, T data) {
        String storageName = getService().getStorage();
        JSON query = JSON.of("uuid", uuid.toString());

        storage.get(storageName, query).thenAccept(result -> {
            if (result == null || result.isEmpty()) {
                storage.add(storageName, gson.toJsonTree(data).getAsJsonObject());
            } else {
                storage.set(storageName, query.get(), gson.toJsonTree(data).getAsJsonObject());
            }
        });
    }

    private void delete(UUID uuid) {
        String storageName = getService().getStorage();
        JSON query = JSON.of("uuid", uuid.toString());

        storage.get(storageName, query).thenAccept(result -> {
            if (result != null && !result.isEmpty()) {
                storage.set(storageName, query, JSON.of());
            }
        });
    }
}
