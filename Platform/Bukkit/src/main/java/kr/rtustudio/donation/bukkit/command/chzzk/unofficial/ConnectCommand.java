package kr.rtustudio.donation.bukkit.command.chzzk.unofficial;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

import java.util.List;

/**
 * 치지직 비공식 연동 명령어
 * <p>
 * 사용법: /후원API 치지직비공식 연동 <스트리머ID>
 */
public class ConnectCommand extends RSCommand<BukkitDonationAPI> {

    public ConnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "connect");
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        if (data.length() < 3) return Result.WRONG_USAGE;

        String streamerId = data.args(2);
        if (streamerId.isEmpty()) return Result.WRONG_USAGE;

        chat().announce(message().get(player(), "service.not_implemented"));
        return Result.FAILURE;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        if (data.length(3)) return List.of("<streamerId>");
        return List.of();
    }
}
