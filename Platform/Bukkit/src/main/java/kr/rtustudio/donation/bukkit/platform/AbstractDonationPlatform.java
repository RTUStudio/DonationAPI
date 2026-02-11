package kr.rtustudio.donation.bukkit.platform;

import com.google.gson.Gson;
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.platform.data.UserData;
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
    protected final PlayerChat chat;
    protected final Gson gson;

    protected final ConcurrentHashMap<UUID, T> connections = new ConcurrentHashMap<>();

    protected AbstractDonationPlatform(BukkitDonationAPI plugin, Gson serializer) {
        this.plugin = plugin;
        this.chat = PlayerChat.of(plugin);
        this.gson = serializer;
    }

    @Override
    public boolean connect(UUID uuid, T data) {
        try {
            connections.put(uuid, data);
            save(uuid, data);
            plugin.getPlayerManager().markConnected(uuid, getService());
            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to connect player " + uuid + " to " + getService() + ": " + e.getMessage());
            return false;
        }
    }

    @Override
    public void disconnect(UUID uuid) {
        connections.remove(uuid);
        deleteFromStorage(uuid);
        plugin.getPlayerManager().markDisconnected(uuid, getService());
    }

    private void deleteFromStorage(UUID uuid) {
        Storage storage = plugin.getStorage();
        String storageName = getService().getStorage();
        JSON query = JSON.of("uuid", uuid.toString());

        storage.get(storageName, query).thenAccept(result -> {
            if (result != null && !result.isEmpty()) {
                storage.set(storageName, query, JSON.of());
            }
        });
    }

    @Override
    public void initialize() {
    }

    @Override
    public void shutdown() {
        connections.clear();
    }

    public T getConnectionData(UUID uuid) {
        return connections.get(uuid);
    }

    public boolean isConnected(UUID uuid) {
        return connections.containsKey(uuid);
    }

    @Override
    public boolean isActive(UUID uuid) {
        return isConnected(uuid) && plugin.getPlayerManager().isActive(uuid, getService());
    }

    protected abstract Class<T> getDataClass();

    @Override
    public void load(UUID uuid) {
        Storage storage = plugin.getStorage();
        storage.get(getService().getStorage(), JSON.of("uuid", uuid.toString()))
                .thenAccept(result -> {
                    if (result.isEmpty()) {
                        plugin.getPlayerManager().resetDonationStatus(uuid, getService());
                        return;
                    }
                    T data = gson.fromJson(result.getFirst(), getDataClass());
                    connections.put(uuid, data);
                    plugin.getPlayerManager().markConnected(uuid, getService());
                });
    }

    protected void save(UUID uuid, T data) {
        Storage storage = plugin.getStorage();
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
}
