package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.command.platform.*;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkOfficialConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SSAPIConfig;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

/**
 * 후원API 메인 명령어
 * <p>
 * /후원API [치지직|숲|SSAPI|투네이션|유튜브]
 */
public class MainCommand extends RSCommand<BukkitDonationAPI> {

    public MainCommand(BukkitDonationAPI plugin) {
        super(plugin, "donation");
        registerCommand(new ChzzkCommand(plugin));
        registerCommand(new SoopGroupCommand(plugin));
        registerCommand(new SSAPICommand(plugin));
        registerCommand(new ToonationCommand(plugin));
        registerCommand(new YoutubeCommand(plugin));
    }

    @Override
    protected void reload(RSCommandData data) {
        getPlugin().reloadConfiguration(SSAPIConfig.class);
        getPlugin().reloadConfiguration(ChzzkOfficialConfig.class);
        getPlugin().reloadServices();
    }
}
