package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.command.chzzk.ChzzkCommand;
import kr.rtustudio.donation.bukkit.command.event.EventCommand;
import kr.rtustudio.donation.bukkit.command.soop.SoopCommand;
import kr.rtustudio.donation.bukkit.command.ssapi.chzzk.SSAPIChzzkCommand;
import kr.rtustudio.donation.bukkit.command.ssapi.soop.SSAPISoopCommand;
import kr.rtustudio.donation.bukkit.command.toonation.ToonationCommand;
import kr.rtustudio.donation.bukkit.command.cime.CimeCommand;
import kr.rtustudio.donation.bukkit.command.youtube.YoutubeCommand;
import kr.rtustudio.donation.bukkit.configuration.service.CimeConfig;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkConfig;
import kr.rtustudio.donation.bukkit.configuration.service.SSAPIConfig;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * 후원API 메인 명령어
 * <p>
 * /후원API [SSAPI치지직|SSAPI숲|치지직|치지직비공식|숲|유튜브|투네이션|이벤트]
 */
public class MainCommand extends RSCommand<BukkitDonationAPI> {

    public MainCommand(BukkitDonationAPI plugin) {
        super(plugin, "donation");
        registerCommand(new SSAPIChzzkCommand(plugin));
        registerCommand(new SSAPISoopCommand(plugin));
        registerCommand(new ChzzkCommand(plugin));
        registerCommand(new SoopCommand(plugin));
        registerCommand(new YoutubeCommand(plugin));
        registerCommand(new ToonationCommand(plugin));
        registerCommand(new CimeCommand(plugin));
        registerCommand(new EventCommand(plugin));
    }

    @Override
    protected void reload(CommandArgs args) {
        plugin.reloadConfiguration(SSAPIConfig.class);
        plugin.reloadConfiguration(ChzzkConfig.class);
        plugin.reloadConfiguration(CimeConfig.class);
        plugin.reloadServices();
    }
}
