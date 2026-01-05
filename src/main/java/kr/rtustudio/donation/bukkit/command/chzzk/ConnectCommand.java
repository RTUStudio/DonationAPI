package kr.rtustudio.donation.bukkit.command.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

public class ConnectCommand extends RSCommand<BukkitDonationAPI> {

    public ConnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "connect");
    }

    @Override
    protected Result execute(RSCommandData data) {
        chat().announce(message().get(player(), "connect.chzzk-pending"));
        return Result.SUCCESS;
    }
}
