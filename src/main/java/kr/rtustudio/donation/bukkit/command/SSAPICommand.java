package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.command.ssapi.ConnectCommand;
import kr.rtustudio.donation.bukkit.command.ssapi.RegisterCommand;
import kr.rtustudio.donation.bukkit.command.ssapi.UnregisterCommand;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

public class SSAPICommand extends RSCommand<BukkitDonationAPI> {

    public SSAPICommand(BukkitDonationAPI plugin, Platform platform) {
        super(plugin, "ssapi-" + platform.name().toLowerCase());
        registerCommand(new RegisterCommand(plugin, platform));
        registerCommand(new UnregisterCommand(plugin, platform));
        registerCommand(new ConnectCommand(plugin, platform));
    }

    @Override
    protected Result execute(RSCommandData data) {
        return Result.WRONG_USAGE;
    }
}
