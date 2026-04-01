package kr.rtustudio.donation.bukkit.command.ssapi.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.command.DisconnectCommand;
import kr.rtustudio.donation.bukkit.command.ssapi.SSAPIConnectCommand;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * SSAPI 치지직 명령어
 * <p>
 * /후원API SSAPI치지직 [연동|연동해제] <스트리머ID>
 */
public class SSAPIChzzkCommand extends RSCommand<BukkitDonationAPI> {

    public SSAPIChzzkCommand(BukkitDonationAPI plugin) {
        super(plugin, "ssapi-chzzk");
        registerCommand(new SSAPIConnectCommand(plugin, Platform.CHZZK));
        registerCommand(new DisconnectCommand(plugin, Services.SSAPI));
    }
}
