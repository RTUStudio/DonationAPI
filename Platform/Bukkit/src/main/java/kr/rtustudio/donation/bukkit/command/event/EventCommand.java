package kr.rtustudio.donation.bukkit.command.event;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.event.DonationEvent;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class EventCommand extends RSCommand<BukkitDonationAPI> {

    public EventCommand(BukkitDonationAPI plugin) {
        super(plugin, "event");
    }

    @Override
    protected Result execute(CommandArgs args) {
        if (!(getSender() instanceof Player player)) return Result.ONLY_PLAYER;

        if (args.length() < 4) return Result.WRONG_USAGE;

        String targetPlayerName = args.get(1);
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            notifier.announce("<red>해당 플레이어를 찾을 수 없습니다.</red>");
            return Result.FAILURE;
        }

        String amountStr = args.get(2);
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            notifier.announce("<red>가격은 숫자로 입력해주세요.</red>");
            return Result.FAILURE;
        }

        String message = String.join(" ", Arrays.copyOfRange(args.args(), 3, args.args().length));

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

        notifier.announce("<green>후원 이벤트가 발생했습니다. (대상: " + targetPlayer.getName() + ")</green>");
        return Result.SUCCESS;
    }

    @Override
    public List<String> tabComplete(CommandArgs args) {
        if (args.length(2)) return Bukkit.getOnlinePlayers().stream().map(Player::getName).toList();
        if (args.length(3)) return List.of("<가격>");
        if (args.length(4)) return List.of("<내용>");
        return List.of();
    }
}
