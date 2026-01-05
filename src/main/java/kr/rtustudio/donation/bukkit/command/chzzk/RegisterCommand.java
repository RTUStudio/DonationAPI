package kr.rtustudio.donation.bukkit.command.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

public class RegisterCommand extends RSCommand<BukkitDonationAPI> {

    public RegisterCommand(BukkitDonationAPI plugin) {
        super(plugin, "register");
    }

    @Override
    protected Result execute(RSCommandData data) {
        chat().announce(message().get(player(), "register.unsupported"));
        return Result.FAILURE;
    }
}
