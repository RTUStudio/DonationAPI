package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.configuration.GlobalConfig;
import kr.rtustudio.donation.bukkit.configuration.SocketConfig;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

public class MainCommand extends RSCommand<BukkitDonationAPI> {

    public MainCommand(BukkitDonationAPI plugin) {
        super(plugin, "donation");
        registerCommand(new RegisterCommand(plugin));
        registerCommand(new UnregisterCommand(plugin));
        registerCommand(new InfoCommand(plugin));
    }

    @Override
    protected void reload(RSCommandData data) {
        getPlugin().reloadConfiguration(GlobalConfig.class);
        getPlugin().reloadConfiguration(SocketConfig.class);
        getPlugin().reloadAPI();
    }

}
