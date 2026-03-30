package kr.rtustudio.donation.bukkit.command.youtube;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.donation.bukkit.manager.PlatformConnectionManager;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.youtube.YoutubeService;
import kr.rtustudio.donation.service.youtube.data.YoutubePlayer;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 유튜브 연동 명령어
 * <p>
 * 사용법: /후원API 유튜브 연동 <핸들명>
 */
public class ConnectCommand extends RSCommand<BukkitDonationAPI> {

    public ConnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "connect");
    }

    @Override
    protected Result execute(CommandArgs args) {
        if (!(getSender() instanceof Player player)) return Result.ONLY_PLAYER;
        if (!args.length(3)) {
            notifier.announce(player, message.get(player, "youtube.usage"));
            return Result.FAILURE;
        }

        String handle = args.get(2);
        PlatformConnectionManager connectionManager = plugin.getConnectionManager();

        // 기존 연결 해제
        connectionManager.disconnect(player.getUniqueId(), Services.Youtube);

        YoutubePlayer data = new YoutubePlayer(player.getUniqueId(), handle);

        YoutubeService service = plugin.getDonationAPI().get(Services.Youtube, YoutubeService.class);
        if (service == null) {
            notifier.announce(player, message.get(player, "service.not_enabled"));
            return Result.FAILURE;
        }

        try {
            boolean success = service.reconnect(player.getUniqueId(), data);
            if (success) {
                notifier.announce(player, message.get(player, "connect.success"));
                return Result.SUCCESS;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to connect YouTube for " + player.getName() + ": " + e.getMessage());
        }
        
        notifier.announce(player, message.get(player, "connect.fail"));
        return Result.FAILURE;
    }

    @Override
    public List<String> tabComplete(CommandArgs args) {
        if (args.length(3)) return List.of("<핸들명>");
        return List.of();
    }
}
