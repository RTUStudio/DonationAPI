package kr.rtustudio.donation.bukkit.command.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.common.Response;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

public class UnregisterCommand extends RSCommand<BukkitDonationAPI> {

    public UnregisterCommand(BukkitDonationAPI plugin) {
        super(plugin, "unregister");
    }

    @Override
    protected Result execute(RSCommandData data) {
        chat().announce(message().get(player(), messageKey(Response.UNSUPPORTED)));
        return Result.FAILURE;
    }

    private String messageKey(Response response) {
        return switch (response) {
            case Response.SUCCESS -> "unregister.success";
            case Response.UNSUPPORTED -> "unregister.unsupported";
            default -> "unregister.fail";
        };
    }
}
