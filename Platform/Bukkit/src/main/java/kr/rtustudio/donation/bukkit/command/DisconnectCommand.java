package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.PlatformConnectionManager;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import org.bukkit.entity.Player;

/**
 * 범용 연동해제 명령어
 * <p>
 * 모든 플랫폼의 연동해제에 공통으로 사용됩니다.
 */
public class DisconnectCommand extends RSCommand<BukkitDonationAPI> {

    private final PlatformConnectionManager connectionManager;
    private final Services service;

    public DisconnectCommand(BukkitDonationAPI plugin, Services service) {
        super(plugin, "disconnect");
        this.connectionManager = plugin.getConnectionManager();
        this.service = service;
    }

    @Override
    protected Result execute(CommandArgs args) {
        if (!(getSender() instanceof Player player)) return Result.ONLY_PLAYER;

        try {
            connectionManager.disconnect(player.getUniqueId(), service);
            notifier.announce(message.get(player, "disconnect.success"));
            return Result.SUCCESS;
        } catch (Exception e) {
            notifier.announce(message.get(player, "disconnect.fail"));
            return Result.FAILURE;
        }
    }
}
