package kr.rtustudio.donation.bukkit.command.platform;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

import java.util.List;

/**
 * 치지직 비공식 연동 명령어
 * <p>
 * 사용법: /후원API 치지직 비공식 <streamerId>
 */
public class ChzzkUnofficialCommand extends RSCommand<BukkitDonationAPI> {

    public ChzzkUnofficialCommand(BukkitDonationAPI plugin) {
        super(plugin, "비공식");
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        if (data.length(2)) return disconnect();

        String streamerId = data.args(2);
        if (streamerId.isEmpty()) return Result.WRONG_USAGE;

        return connect(streamerId);
    }

    private Result connect(String streamerId) {
        chat().announce(message().get(player(), "service.not_implemented"));
        return Result.FAILURE;
    }

    private Result disconnect() {
        try {
            getPlugin().getConnectionManager().disconnect(player().getUniqueId(), Services.ChzzkUnofficial);
            chat().announce(message().get(player(), "disconnect.success"));
            return Result.SUCCESS;
        } catch (Exception e) {
            chat().announce(message().get(player(), "disconnect.fail"));
            return Result.FAILURE;
        }
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        if (data.length(3)) return List.of("<streamerId>");
        return List.of();
    }
}
