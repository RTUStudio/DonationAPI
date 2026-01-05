package kr.rtustudio.donation.bukkit.command.ssapi;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.common.Response;
import kr.rtustudio.donation.service.ssapi.SSAPIService;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

import java.util.List;

public class RegisterCommand extends RSCommand<BukkitDonationAPI> {

    private final Platform platform;

    public RegisterCommand(BukkitDonationAPI plugin, Platform platform) {
        super(plugin, "register");
        this.platform = platform;
    }

    @Override
    protected Result execute(RSCommandData data) {
        String user = data.args(2);
        if (user.isEmpty()) return Result.FAILURE;

        SSAPIService api = getPlugin().getDonationAPI().getSSAPI();
        if (api == null) return Result.FAILURE;

        api.register(platform, user)
                .thenAccept(response -> chat().announce(message().get(player(), messageKey(response))));
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        if (data.length(3)) return List.of("<id>");
        return List.of();
    }


    private String messageKey(Response response) {
        return switch (response) {
            case Response.SUCCESS -> "register.success";
            case Response.UNSUPPORTED -> "register.unsupported";
            default -> "register.fail";
        };
    }
}
