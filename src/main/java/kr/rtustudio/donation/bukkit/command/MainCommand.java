package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SSAPIConfig;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

import java.util.List;

public class MainCommand extends RSCommand<BukkitDonationAPI> {

    public MainCommand(BukkitDonationAPI plugin) {
        super(plugin, List.of("donation", "후원API"));
        registerCommand(new SSAPICommand(plugin, Platform.CHZZK));
        registerCommand(new SSAPICommand(plugin, Platform.SOOP));
        registerCommand(new ChzzkCommand(plugin));
    }

    @Override
    protected void reload(RSCommandData data) {
        getPlugin().reloadConfiguration(SSAPIConfig.class);
        getPlugin().reloadConfiguration(ChzzkConfig.class);
        getPlugin().reloadServices();
    }

}
