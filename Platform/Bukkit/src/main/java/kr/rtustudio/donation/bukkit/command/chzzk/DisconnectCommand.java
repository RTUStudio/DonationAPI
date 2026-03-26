package kr.rtustudio.donation.bukkit.command.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.PlatformConnectionManager;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import org.bukkit.entity.Player;

/**
 * 치지직 공식 연동해제 명령어
 * <p>
 * 사용법: /후원API 치지직 연동해제
 */
public class DisconnectCommand extends RSCommand<BukkitDonationAPI> {

    private final PlatformConnectionManager connectionManager;

    public DisconnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "disconnect");
        this.connectionManager = plugin.getConnectionManager();
    }

    @Override
    protected Result execute(CommandArgs args) {
        if (!(getSender() instanceof Player player)) return Result.ONLY_PLAYER;

        try {
            connectionManager.disconnect(player.getUniqueId(), Services.Chzzk);
            notifier.announce(message.get(player, "disconnect.success"));
            return Result.SUCCESS;
        } catch (Exception e) {
            notifier.announce(message.get(player, "disconnect.fail"));
            return Result.FAILURE;
        }
    }
}
