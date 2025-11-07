package kr.rtustudio.donation.bukkit.command;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.common.data.Platform;
import kr.rtustudio.donation.common.data.ResponseResult;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.api.scheduler.CraftScheduler;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UnregisterCommand extends RSCommand<BukkitDonationAPI> {

    public UnregisterCommand(BukkitDonationAPI plugin) {
        super(plugin, "unregister");
    }

    @Override
    public Result execute(RSCommandData data) {
        String platformArg = data.args(1);
        String user = data.args(2);

        if (platformArg.isEmpty() || user.isEmpty()) return Result.FAILURE;

        Platform platform;
        try {
            platform = Platform.valueOf(platformArg.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return Result.FAILURE;
        }

        CompletableFuture<ResponseResult> result = getPlugin().getAPI().unregister(platform, user);
        result.thenAccept(responseResult -> CraftScheduler.sync(() -> {
            boolean ok = responseResult.succeeded();
            String key = ok ? "unregister.success" : "unregister.fail";
            chat().announce(message().get(player(), key));
            if (ok) return;
            String extra = responseResult.message();
            if (extra != null && !extra.isEmpty()) {
                chat().announce(message().get(player(), "reason").replace("{message}", extra));
            }
        }));
        return Result.SUCCESS;
    }

    @Override
    public List<String> tabComplete(RSCommandData data) {
        if (data.length(2)) return Arrays.stream(Platform.values())
                .map(Platform::lowercase).toList();
        return List.of();
    }

}
