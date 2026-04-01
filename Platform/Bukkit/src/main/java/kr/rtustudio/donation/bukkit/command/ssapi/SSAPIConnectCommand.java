package kr.rtustudio.donation.bukkit.command.ssapi;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.common.Response;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.ssapi.SSAPIService;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * SSAPI 범용 연동 명령어
 * <p>
 * Platform 값만 주입받아 SSAPI 연동을 수행합니다.
 */
public class SSAPIConnectCommand extends RSCommand<BukkitDonationAPI> {

    private final Platform platform;

    public SSAPIConnectCommand(BukkitDonationAPI plugin, Platform platform) {
        super(plugin, "connect");
        this.platform = platform;
    }

    @Override
    protected Result execute(CommandArgs args) {
        if (!(getSender() instanceof Player player)) return Result.ONLY_PLAYER;
        if (args.length() < 3) return Result.WRONG_USAGE;

        String streamerId = args.get(2);
        if (streamerId.isEmpty()) return Result.WRONG_USAGE;

        SSAPIService api = getPlugin().getDonationAPI().get(Services.SSAPI, SSAPIService.class);
        if (api == null) {
            notifier.announce(message.get(player, "service.unavailable"));
            return Result.FAILURE;
        }

        api.register(player.getUniqueId(), platform, streamerId).thenAccept(response -> {
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
        if (args.length(3)) return List.of("<id>");
        return List.of();
    }
}
