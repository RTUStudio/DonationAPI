package kr.rtustudio.donation.bukkit.command.ssapi.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.common.Response;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.ssapi.SSAPIService;

import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * SSAPI 치지직 연동 명령어
 * <p>
 * 사용법: /후원API SSAPI치지직 연동 <스트리머ID>
 */
public class ConnectCommand extends RSCommand<BukkitDonationAPI> {

    public ConnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "connect");
    }

    @Override
    protected Result execute(CommandArgs args) {
        if (!(getSender() instanceof Player player)) return Result.ONLY_PLAYER;

        if (args.length() < 3) return Result.WRONG_USAGE;

        String streamerId = args.get(2);
        if (streamerId.isEmpty()) return Result.WRONG_USAGE;

        return connect(streamerId, player);
    }

    private Result connect(String streamerId, Player player) {
        SSAPIService api = getPlugin().getDonationAPI().get(Services.SSAPI, SSAPIService.class);
        if (api == null) {
            notifier.announce(message.get(player, "service.unavailable"));
            return Result.FAILURE;
        }

        api.register(player.getUniqueId(), Platform.CHZZK, streamerId).thenAccept(response -> {
            String messageKey = switch (response) {
                case Response.SUCCESS -> "connect.success";
                case Response.UNSUPPORTED -> "connect.unsupported";
                default -> "connect.fail";
            };
            notifier.announce(message.get(player, messageKey));
        }).exceptionally(throwable -> {
            notifier.announce(message.get(player, "connect.fail"));
            return null;
        });

        return Result.SUCCESS;
    }


    @Override
    public List<String> tabComplete(CommandArgs args) {
        if (args.length(3)) return List.of("<channelId>");
        return List.of();
    }
}
