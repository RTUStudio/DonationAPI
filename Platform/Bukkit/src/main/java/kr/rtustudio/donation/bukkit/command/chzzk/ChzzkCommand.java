package kr.rtustudio.donation.bukkit.command.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * 치지직 공식 명령어
 * <p>
 * /후원API 치지직 [연동|연동해제]
 */
public class ChzzkCommand extends RSCommand<BukkitDonationAPI> {

    public ChzzkCommand(BukkitDonationAPI plugin) {
        super(plugin, "chzzk");
        registerCommand(new ConnectCommand(plugin));
        registerCommand(new DisconnectCommand(plugin));
    }
}
