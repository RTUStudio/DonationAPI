package kr.rtustudio.donation.bukkit;

import kr.rtustudio.donation.bukkit.command.MainCommand;
import kr.rtustudio.donation.bukkit.configuration.GlobalConfig;
import kr.rtustudio.donation.bukkit.configuration.SocketConfig;
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import kr.rtustudio.donation.common.DonationAPI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;

public class BukkitDonationAPI extends RSPlugin {

    @Getter
    private static BukkitDonationAPI instance;

    @Getter
    private DonationAPI API;

    public BukkitDonationAPI() {
        super("ko_kr");
    }

    @Override
    protected void enable() {
        instance = this;

        registerConfiguration(GlobalConfig.class, "Global");
        registerConfiguration(SocketConfig.class, "Socket");

        registerCommand(new MainCommand(this), true);

        reloadAPI();
    }

    @Override
    public void disable() {
        if (API != null) API.close();
    }

    public void reloadAPI() {
        GlobalConfig globalConfig = getConfiguration(GlobalConfig.class);
        SocketConfig socketConfig = getConfiguration(SocketConfig.class);
        if (API != null) API.close();
        API = new DonationAPI(globalConfig.getApiKey(), socketConfig, donation -> {
            verbose(donation.toString());
            CraftScheduler.sync(() -> Bukkit.getPluginManager().callEvent(new DonationEvent(donation)));
        });
    }

}
