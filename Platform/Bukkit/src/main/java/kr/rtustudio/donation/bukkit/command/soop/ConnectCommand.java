package kr.rtustudio.donation.bukkit.command.soop;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

/**
 * 숲 연동 명령어
 * <p>
 * 사용법: /후원API 숲 연동
 */
public class ConnectCommand extends RSCommand<BukkitDonationAPI> {

    public ConnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "connect");
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        chat().announce(message().get(player(), "service.not_implemented"));
        return Result.FAILURE;
    }
}
