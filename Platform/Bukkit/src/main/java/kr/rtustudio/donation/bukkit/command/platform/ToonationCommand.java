package kr.rtustudio.donation.bukkit.command.platform;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

import java.util.List;

/**
 * 투네이션 연동 명령어
 * <p>
 * 사용법: /후원API 투네이션 <address>
 */
public class ToonationCommand extends RSCommand<BukkitDonationAPI> {

    public ToonationCommand(BukkitDonationAPI plugin) {
        super(plugin, "투네이션");
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        if (data.length(1)) return disconnect();

        String address = data.args(1);
        if (address.isEmpty()) return Result.WRONG_USAGE;

        return connect(address);
    }

    private Result connect(String address) {
        chat().announce(message().get(player(), "service.not_implemented"));
        return Result.FAILURE;
    }

    private Result disconnect() {
        try {
            getPlugin().getConnectionManager().disconnect(player().getUniqueId(), Services.Toonation);
            chat().announce(message().get(player(), "disconnect.success"));
            return Result.SUCCESS;
        } catch (Exception e) {
            chat().announce(message().get(player(), "disconnect.fail"));
            return Result.FAILURE;
        }
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        if (data.length(2)) return List.of("<address>");
        return List.of();
    }
}
