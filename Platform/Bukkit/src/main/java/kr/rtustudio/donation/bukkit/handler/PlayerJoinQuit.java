package kr.rtustudio.donation.bukkit.handler;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.listener.RSListener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuit extends RSListener<BukkitDonationAPI> {

    public PlayerJoinQuit(BukkitDonationAPI plugin) {
        super(plugin);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        getPlugin().getDonationManager().load(event.getPlayer().getUniqueId());
        getPlugin().getConnectionManager().loadAll(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();
        getPlugin().disconnectServices(uuid);
        getPlugin().getDonationManager().unload(uuid);
    }
}
