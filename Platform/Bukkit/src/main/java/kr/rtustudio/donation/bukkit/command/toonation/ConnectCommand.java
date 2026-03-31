package kr.rtustudio.donation.bukkit.command.toonation;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.toonation.data.ToonationPlayer;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 투네이션 연동 명령어
 * <p>
 * 사용법: /후원API 투네이션 연동 <alertKey>
 */
public class ConnectCommand extends RSCommand<BukkitDonationAPI> {

    public ConnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "connect");
    }

    @Override
    protected Result execute(CommandArgs args) {
        if (!(getSender() instanceof Player player)) return Result.ONLY_PLAYER;
        if (!args.length(3)) {
            notifier.announce(message.get(player, "toonation.usage"));
            return Result.FAILURE;
        }

        String key = args.get(2);

        // 링크 형식이면 마지막 경로 추출
        if (key.contains("/")) {
            key = key.substring(key.lastIndexOf('/') + 1);
            if (key.contains("?")) key = key.substring(0, key.indexOf('?'));
        }

        ToonationPlayer data = new ToonationPlayer(player.getUniqueId(), key, "");

        plugin.getConnectionManager().connect(player.getUniqueId(), Services.Toonation, data);
        return Result.SUCCESS;
    }

    @Override
    public List<String> tabComplete(CommandArgs args) {
        if (args.length(3)) return List.of("<url>");
        return List.of();
    }
}
