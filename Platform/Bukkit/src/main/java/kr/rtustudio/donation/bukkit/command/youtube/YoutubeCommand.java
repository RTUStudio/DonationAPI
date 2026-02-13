package kr.rtustudio.donation.bukkit.command.youtube;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * 유튜브 명령어
 * <p>
 * /후원API 유튜브 [연동|연동해제] <address>
 */
public class YoutubeCommand extends RSCommand<BukkitDonationAPI> {

    public YoutubeCommand(BukkitDonationAPI plugin) {
        super(plugin, "youtube");
        registerCommand(new ConnectCommand(plugin));
        registerCommand(new DisconnectCommand(plugin));
    }
}
