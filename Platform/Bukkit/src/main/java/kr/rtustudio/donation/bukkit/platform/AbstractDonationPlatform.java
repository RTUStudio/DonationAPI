package kr.rtustudio.donation.bukkit.platform;

import com.google.gson.Gson;
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.DonationManager;
import kr.rtustudio.donation.service.data.UserData;
import kr.rtustudio.storage.JSON;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import kr.rtustudio.storage.Storage;
import com.google.gson.JsonObject;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 후원 플랫폼 추상 클래스
 * <p>
 * {@link DonationPlatform} 인터페이스의 기본 구현을 제공합니다.
 * 연결 관리, 저장소 처리, 플레이어 알림 등의 공통 기능을 구현합니다.
 *
 * @param <T> 플랫폼별 연결 데이터 타입 (UserData를 구현해야 함)
 */
public abstract class AbstractDonationPlatform<T extends UserData> implements DonationPlatform<T> {

    protected final BukkitDonationAPI plugin;
    protected final DonationManager donationManager;
    protected final Notifier notifier;
    protected final Gson gson;

    protected final ConcurrentHashMap<UUID, T> connections = new ConcurrentHashMap<>();

    protected AbstractDonationPlatform(BukkitDonationAPI plugin, Gson serializer) {
        this.plugin = plugin;
        this.donationManager = plugin.getDonationManager();
        this.notifier = Notifier.of(plugin);
        this.gson = serializer;
    }

    protected Storage getStorage() {
        return plugin.getStorage(getService().getStorage());
    }

    @Override
    public boolean connect(UUID uuid, T data) {
        try {
            connections.put(uuid, data);
            save(uuid, data);
            donationManager.markConnected(uuid, getService());
            onRegister(uuid, data);
            plugin.getLogger().fine("%s connected to %s(%s)".formatted(uuid, getService(), data.platform()));
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
        donationManager.markDisconnected(uuid, getService());
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

    public Map<UUID, T> getConnections() {
        return connections;
    }

    public boolean isConnected(UUID uuid) {
        return connections.containsKey(uuid);
    }

    @Override
    public boolean isActive(UUID uuid) {
        return isConnected(uuid) && donationManager.isActive(uuid, getService());
    }

    protected abstract Class<T> dataClass();

    public void load(UUID uuid) {
        getStorage().get(JSON.of("uuid", uuid.toString()))
                .thenAccept(result -> {
                    if (result.isEmpty()) {
                        donationManager.resetDonationStatus(uuid, getService());
                        return;
                    }
                    try {
                        T data = gson.fromJson(result.getFirst(), dataClass());
                        if (data == null) {
                            plugin.getLogger().warning("[%s] Failed to deserialize data for %s".formatted(getService(), uuid));
                            donationManager.resetDonationStatus(uuid, getService());
                            return;
                        }
                        plugin.getLogger().info("[%s] Reconnecting %s...".formatted(getService(), uuid));
                        if (onReconnect(uuid, data)) {
                            donationManager.markConnected(uuid, getService());
                            plugin.getLogger().info("[%s] Reconnected %s successfully".formatted(getService(), uuid));
                        } else {
                            connections.remove(uuid);
                            donationManager.resetDonationStatus(uuid, getService());
                            plugin.getLogger().warning("[%s] Reconnect failed for %s".formatted(getService(), uuid));
                            sendTokenExpiredMessage(uuid);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("[%s] Exception during reconnect for %s: %s".formatted(getService(), uuid, e.getMessage()));
                        donationManager.resetDonationStatus(uuid, getService());
                        sendTokenExpiredMessage(uuid);
                    }
                });
    }

    private void sendTokenExpiredMessage(UUID uuid) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) return;
            String serviceName = plugin.getConfiguration().getMessage()
                    .get(player, "service_name." + getService().name());
            String msg = plugin.getConfiguration().getMessage().get(player, "connection.token_expired")
                    .replace("{service}", serviceName);
            notifier.announce(player, msg);
        });
    }

    protected void onRegister(UUID uuid, T data) {
    }

    protected boolean onReconnect(UUID uuid, T data) {
        return true;
    }

    public void save(UUID uuid, T data) {
        JSON query = JSON.of("uuid", uuid.toString());
        JsonObject jsonData = gson.toJsonTree(data).getAsJsonObject();

        getStorage().get(query).thenAccept(result -> {
            if (result == null || result.isEmpty()) {
                getStorage().add(jsonData);
            } else {
                getStorage().set(query.get(), jsonData);
            }
        });
    }

    private void delete(UUID uuid) {
        JSON query = JSON.of("uuid", uuid.toString());

        getStorage().get(query).thenAccept(result -> {
            if (result != null && !result.isEmpty()) {
                getStorage().set(query, JSON.of());
            }
        });
    }
}
