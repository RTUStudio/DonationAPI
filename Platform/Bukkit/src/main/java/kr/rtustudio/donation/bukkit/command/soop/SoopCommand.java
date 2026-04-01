package kr.rtustudio.donation.bukkit.command.soop;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.command.DisconnectCommand;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * 숲 명령어
 * <p>
 * /후원API 숲 [연동|연동해제]
 */
public class SoopCommand extends RSCommand<BukkitDonationAPI> {

    public SoopCommand(BukkitDonationAPI plugin) {
        super(plugin, "soop");
        registerCommand(new ConnectCommand(plugin));
        registerCommand(new DisconnectCommand(plugin, Services.SOOP));
    }
}
