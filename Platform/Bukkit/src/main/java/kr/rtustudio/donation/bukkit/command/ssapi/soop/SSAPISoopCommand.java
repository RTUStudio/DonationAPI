package kr.rtustudio.donation.bukkit.command.ssapi.soop;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.command.DisconnectCommand;
import kr.rtustudio.donation.bukkit.command.ssapi.SSAPIConnectCommand;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * SSAPI 숲 명령어
 * <p>
 * /후원API SSAPI숲 [연동|연동해제] <스트리머ID>
 */
public class SSAPISoopCommand extends RSCommand<BukkitDonationAPI> {

    public SSAPISoopCommand(BukkitDonationAPI plugin) {
        super(plugin, "ssapi-soop");
        registerCommand(new SSAPIConnectCommand(plugin, Platform.SOOP));
        registerCommand(new DisconnectCommand(plugin, Services.SSAPI));
    }
}
