package kr.rtustudio.donation.bukkit.command.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.command.chzzk.unofficial.ConnectCommand;
import kr.rtustudio.donation.bukkit.command.chzzk.unofficial.DisconnectCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;

/**
 * 치지직 비공식 명령어
 * <p>
 * /후원API 치지직비공식 [연동|연동해제] <스트리머ID>
 */
public class UnofficialCommand extends RSCommand<BukkitDonationAPI> {

    public UnofficialCommand(BukkitDonationAPI plugin) {
        super(plugin, "chzzk-unofficial");
        registerCommand(new ConnectCommand(plugin));
        registerCommand(new DisconnectCommand(plugin));
    }
}
