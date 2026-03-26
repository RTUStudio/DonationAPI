package kr.rtustudio.donation.bukkit.command.cime;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * 씨미(Cime) 명령어
 * <p>
 * /후원API cime [연동|연동해제] <alertKey>
 */
public class CimeCommand extends RSCommand<BukkitDonationAPI> {

    public CimeCommand(BukkitDonationAPI plugin) {
        super(plugin, "cime");
        registerCommand(new ConnectCommand(plugin));
        registerCommand(new DisconnectCommand(plugin));
    }
}
