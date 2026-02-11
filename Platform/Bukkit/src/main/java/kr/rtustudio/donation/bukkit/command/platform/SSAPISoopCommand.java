package kr.rtustudio.donation.bukkit.command.platform;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.platform.data.SSAPIData;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.common.Response;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.ssapi.SSAPIService;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

import java.util.List;

/**
 * SSAPI 숲 연동 명령어
 * <p>
 * 사용법: /후원API SSAPI 숲 <streamerId>
 */
public class SSAPISoopCommand extends RSCommand<BukkitDonationAPI> {

    public SSAPISoopCommand(BukkitDonationAPI plugin) {
        super(plugin, "숲");
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        if (data.length(2)) return disconnect();

        String streamerId = data.args(2);
        if (streamerId.isEmpty()) return Result.WRONG_USAGE;

        return connect(streamerId);
    }

    private Result connect(String streamerId) {
        SSAPIService api = getPlugin().getDonationAPI().getSSAPI();
        if (api == null) {
            chat().announce(message().get(player(), "service.unavailable"));
            return Result.FAILURE;
        }

        api.register(Platform.SOOP, streamerId).thenAccept(response -> {
            String messageKey = switch (response) {
                case Response.SUCCESS -> {
                    saveConnection(streamerId);
                    yield "connect.success";
                }
                case Response.UNSUPPORTED -> "connect.unsupported";
                default -> "connect.fail";
            };
            chat().announce(message().get(player(), messageKey));
        }).exceptionally(throwable -> {
            chat().announce(message().get(player(), "connect.fail"));
            return null;
        });

        return Result.SUCCESS;
    }

    private void saveConnection(String streamerId) {
        SSAPIData connectionData = new SSAPIData(
                player().getUniqueId(),
                streamerId,
                Platform.SOOP
        );
        boolean success = getPlugin().getConnectionManager().connect(
                player().getUniqueId(),
                Services.SSAPI,
                connectionData
        );

        if (success) {
            String msg = message().get(player(), "connection.success")
                    .replace("{service}", Services.SSAPI.name())
                    .replace("{streamer}", streamerId);
            chat().announce(msg);
        } else {
            chat().announce(message().get(player(), "connect.fail"));
        }
    }

    private Result disconnect() {
        try {
            getPlugin().getConnectionManager().disconnect(player().getUniqueId(), Services.SSAPI);
            chat().announce(message().get(player(), "disconnect.success"));
            return Result.SUCCESS;
        } catch (Exception e) {
            chat().announce(message().get(player(), "disconnect.fail"));
            return Result.FAILURE;
        }
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        if (data.length(3)) return List.of("<streamerId>");
        return List.of();
    }
}
