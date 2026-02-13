package kr.rtustudio.donation.bukkit.command.chzzk.official;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

/**
 * 치지직 공식 연동해제 명령어
 * <p>
 * 사용법: /후원API 치지직 연동해제
 */
public class DisconnectCommand extends RSCommand<BukkitDonationAPI> {

    public DisconnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "disconnect");
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        try {
            getPlugin().getConnectionManager().disconnect(player().getUniqueId(), Services.ChzzkOfficial);
            chat().announce(message().get(player(), "disconnect.success"));
            return Result.SUCCESS;
        } catch (Exception e) {
            chat().announce(message().get(player(), "disconnect.fail"));
            return Result.FAILURE;
        }
    }
}
