package kr.rtustudio.donation.bukkit;

import kr.rtustudio.donation.bukkit.command.MainCommand;
import kr.rtustudio.donation.bukkit.configuration.GlobalConfig;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SSAPIConfig;
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationAPI;
import kr.rtustudio.framework.bukkit.api.RSPlugin;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;
import lombok.Getter;
import org.bukkit.Bukkit;

import java.util.function.Consumer;

public class BukkitDonationAPI extends RSPlugin {

    @Getter
    private static BukkitDonationAPI instance;

    @Getter
    private DonationAPI donationAPI;

    public BukkitDonationAPI() {
        super("ko_kr");
    }

    @Override
    protected void enable() {
        instance = this;

        initStorage("Key");

        registerConfiguration(GlobalConfig.class, "Global");
        registerConfiguration(SSAPIConfig.class, "Configs/Services", "SSAPI");
        registerConfiguration(ChzzkConfig.class, "Configs/Services", "Chzzk");

        registerCommand(new MainCommand(this), true);
        setupServices();
    }

    @Override
    public void disable() {
        if (donationAPI != null) donationAPI.close();
    }

    public void reloadServices() {
        if (donationAPI != null) donationAPI.close();
        setupServices();
    }

    private void setupServices() {
        donationAPI = new DonationAPI();
        Consumer<Donation> handler = donation -> {
            verbose(donation.toString());
            CraftScheduler.sync(() -> Bukkit.getPluginManager().callEvent(new DonationEvent(donation)));
        };
        donationAPI.startSSAPI(getConfiguration(SSAPIConfig.class), handler);
        donationAPI.startChzzk(getConfiguration(ChzzkConfig.class), handler);
    }
}
