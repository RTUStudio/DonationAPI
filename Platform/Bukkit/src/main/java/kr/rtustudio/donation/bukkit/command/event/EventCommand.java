package kr.rtustudio.donation.bukkit.command.event;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.RSCommandData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class EventCommand extends RSCommand<BukkitDonationAPI> {

    public EventCommand(BukkitDonationAPI plugin) {
        super(plugin, "event");
    }

    @Override
    protected Result execute(RSCommandData data) {
        if (player() == null) return Result.ONLY_PLAYER;

        if (data.length() < 4) return Result.WRONG_USAGE;

        String targetPlayerName = data.args(1);
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            chat().announce("<red>해당 플레이어를 찾을 수 없습니다.</red>");
            return Result.FAILURE;
        }

        String amountStr = data.args(2);
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            chat().announce("<red>가격은 숫자로 입력해주세요.</red>");
            return Result.FAILURE;
        }

        String message = String.join(" ", Arrays.copyOfRange(data.args(), 3, data.args().length));

        Donation donation = new Donation(
                targetPlayer.getUniqueId(),
                Services.Chzzk,
                Platform.CHZZK,
                DonationType.CHAT,
                targetPlayer.getName(),
                "DonationAPI",
                "DonationAPI",
                message,
                amount
        );

        DonationEvent event = new DonationEvent(targetPlayer, donation);
        Bukkit.getPluginManager().callEvent(event);

        chat().announce("<green>후원 이벤트가 발생했습니다. (대상: " + targetPlayer.getName() + ")</green>");
        return Result.SUCCESS;
    }

    @Override
    protected List<String> tabComplete(RSCommandData data) {
        if (data.length(2)) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        if (data.length(3)) return List.of("<가격>");
        if (data.length(4)) return List.of("<내용>");
        return List.of();
    }
}
