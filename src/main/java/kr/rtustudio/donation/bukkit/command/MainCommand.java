package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SSAPIConfig;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

public class MainCommand extends RSCommand<BukkitDonationAPI> {

    public MainCommand(BukkitDonationAPI plugin) {
        super(plugin, "donation");
        //registerCommand(new RegisterCommand(plugin));
        //registerCommand(new UnregisterCommand(plugin));
        //registerCommand(new InfoCommand(plugin));
    }

    @Override
    protected void reload(RSCommandData data) {
        getPlugin().reloadConfiguration(SSAPIConfig.class);
        getPlugin().reloadConfiguration(ChzzkConfig.class);
        getPlugin().reloadServices();
    }

}
