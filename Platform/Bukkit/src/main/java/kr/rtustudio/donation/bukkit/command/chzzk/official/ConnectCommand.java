package kr.rtustudio.donation.bukkit.command.chzzk.official;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkConfig;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;

/**
 * 치지직 공식 연동 명령어
 * <p>
 * 사용법: /후원API 치지직 연동
 */
public class ConnectCommand extends RSCommand<BukkitDonationAPI> {

    public ConnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "connect");
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        ChzzkConfig config = getPlugin().getConfiguration(ChzzkConfig.class);
        if (config == null || !config.isEnabled()) {
            chat().announce(message().get(player(), "service.unavailable"));
            return Result.FAILURE;
        }

        String authUrl = config.getBaseUri() + "/auth/login/chzzk?user=" + player().getUniqueId();

        chat().announce(message().get(player(), "chzzk.connect.warning"));
        chat().announce("<click:copy_to_clipboard:'" + authUrl + "'>" + message().get(player(), "chzzk.connect.link") + "</click>");

        return Result.SUCCESS;
    }
}
