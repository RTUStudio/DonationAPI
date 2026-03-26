package kr.rtustudio.donation.bukkit.handler;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.DonationManager;
import kr.rtustudio.donation.bukkit.manager.PlatformConnectionManager;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class PlayerJoinQuit extends RSListener<BukkitDonationAPI> {

    private final DonationManager donationManager;
    private final PlatformConnectionManager connectionManager;

    public PlayerJoinQuit(BukkitDonationAPI plugin) {
        super(plugin);
        this.donationManager = plugin.getDonationManager();
        this.connectionManager = plugin.getConnectionManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        donationManager.load(uuid)
                .thenRun(() -> connectionManager.loadAll(uuid));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        getPlugin().disconnectServices(uuid);
        donationManager.unload(uuid);
    }
}
