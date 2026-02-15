package kr.rtustudio.donation.bukkit.command.chzzk;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.configuration.service.ChzzkConfig;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import kr.rtustudio.framework.bukkit.api.format.ComponentFormatter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;

/**
 * 치지직 공식 연동 명령어
 * <p>
 * 사용법: /후원API 치지직 연동
 */
public class ConnectCommand extends RSCommand<BukkitDonationAPI> {

    private final ChzzkConfig config;

    public ConnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "connect");
        this.config = plugin.getConfiguration(ChzzkConfig.class);
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        if (!config.isEnabled()) {
            chat().announce(message().get(player(), "service.unavailable"));
            return Result.FAILURE;
        }

        String authUrl = config.getBaseUri() + "/auth/login/chzzk?user=" + player().getUniqueId();

        chat().announce(message().get(player(), "chzzk.connect.warning"));

        Component linkMessage = ComponentFormatter.mini(message().get(player(), "chzzk.connect.link"))
                .clickEvent(ClickEvent.copyToClipboard(authUrl))
                .hoverEvent(HoverEvent.showText(ComponentFormatter.mini(message().get(player(), "chzzk.connect.copied"))));
        chat().announce(linkMessage);

        return Result.SUCCESS;
    }
}
