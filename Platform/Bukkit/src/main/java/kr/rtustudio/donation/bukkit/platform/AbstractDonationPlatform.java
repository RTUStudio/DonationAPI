package kr.rtustudio.donation.bukkit.platform;

import com.google.gson.Gson;
import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.DonationManager;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.data.UserData;
import kr.rtustudio.storage.JSON;
import kr.rtustudio.framework.bukkit.api.player.Notifier;
import kr.rtustudio.storage.Storage;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
            announce(uuid, data);
            onRegister(uuid, data);
            plugin.getLogger().info("%s connected to %s(%s)".formatted(uuid, getService(), data.platform()));
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

    public ConcurrentHashMap<UUID, T> getConnections() {
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
                    T data = gson.fromJson(result.getFirst(), dataClass());
                    if (onReconnect(uuid, data)) {
                        connections.put(uuid, data);
                        donationManager.markConnected(uuid, getService());
                    } else {
                        connections.remove(uuid);
                        donationManager.resetDonationStatus(uuid, getService());
                    }
                });
    }

    private void announce(UUID uuid, T data) {
        Player player = Bukkit.getPlayer(uuid);
        if (player == null || !player.isOnline()) return;
        announce(player, getService(), data.platform(), data.channelId());
    }

    private void announce(Player player, Services service, Platform platform, String streamerId) {
        String message = plugin.getConfiguration().getMessage()
                .get(player, "connection.success")
                .replace("{service}", service.name())
                .replace("{platform}", platform.name())
                .replace("{id}", streamerId);
        notifier.announce(player, message);
    }

    protected void onRegister(UUID uuid, T data) {
    }

    protected boolean onReconnect(UUID uuid, T data) {
        return true;
    }

    protected void save(UUID uuid, T data) {
        JSON query = JSON.of("uuid", uuid.toString());

        getStorage().get(query).thenAccept(result -> {
            if (result == null || result.isEmpty()) {
                getStorage().add(gson.toJsonTree(data).getAsJsonObject());
            } else {
                getStorage().set(query.get(), gson.toJsonTree(data).getAsJsonObject());
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
