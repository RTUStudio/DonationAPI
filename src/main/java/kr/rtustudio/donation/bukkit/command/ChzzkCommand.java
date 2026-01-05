package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.command.chzzk.ConnectCommand;
import kr.rtustudio.donation.bukkit.command.chzzk.RegisterCommand;
import kr.rtustudio.donation.bukkit.command.chzzk.UnregisterCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

public class ChzzkCommand extends RSCommand<BukkitDonationAPI> {

    public ChzzkCommand(BukkitDonationAPI plugin) {
        super(plugin, "chzzk");
        registerCommand(new RegisterCommand(plugin));
        registerCommand(new UnregisterCommand(plugin));
        registerCommand(new ConnectCommand(plugin));
    }

    @Override
    protected Result execute(RSCommandData data) {
        return Result.WRONG_USAGE;
    }
}
