package kr.rtustudio.donation.bukkit.command.platform;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

/**
 * 숲 공식 연동 명령어
 * <p>
 * 사용법: /후원API 숲 연동
 */
public class SoopCommand extends RSCommand<BukkitDonationAPI> {

    public SoopCommand(BukkitDonationAPI plugin) {
        super(plugin, "연동");
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        if (data.length(2)) {
            chat().announce(message().get(player(), "service.not_implemented"));
            return Result.FAILURE;
        }

        if (data.equalsIgnoreCase(2, "해제")) return disconnect();
        return Result.WRONG_USAGE;
    }

    private Result disconnect() {
        try {
            getPlugin().getConnectionManager().disconnect(player().getUniqueId(), Services.SOOP);
            chat().announce(message().get(player(), "disconnect.success"));
            return Result.SUCCESS;
        } catch (Exception e) {
            chat().announce(message().get(player(), "disconnect.fail"));
            return Result.FAILURE;
        }
    }
}
