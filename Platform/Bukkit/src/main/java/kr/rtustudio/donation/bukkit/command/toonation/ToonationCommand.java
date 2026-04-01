package kr.rtustudio.donation.bukkit.command.toonation;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.command.DisconnectCommand;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * 투네이션 명령어
 * <p>
 * /후원API 투네이션 [연동|연동해제] <address>
 */
public class ToonationCommand extends RSCommand<BukkitDonationAPI> {

    public ToonationCommand(BukkitDonationAPI plugin) {
        super(plugin, "toonation");
        registerCommand(new ConnectCommand(plugin));
        registerCommand(new DisconnectCommand(plugin, Services.Toonation));
    }
}
