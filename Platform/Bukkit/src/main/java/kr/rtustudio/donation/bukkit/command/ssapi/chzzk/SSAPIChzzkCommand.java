package kr.rtustudio.donation.bukkit.command.ssapi.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * SSAPI 치지직 명령어
 * <p>
 * /후원API SSAPI치지직 [연동|연동해제] <스트리머ID>
 */
public class SSAPIChzzkCommand extends RSCommand<BukkitDonationAPI> {

    public SSAPIChzzkCommand(BukkitDonationAPI plugin) {
        super(plugin, "ssapi-chzzk");
        registerCommand(new ConnectCommand(plugin));
        registerCommand(new DisconnectCommand(plugin));
    }
}
