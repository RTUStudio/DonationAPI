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
        if (!(getSender() instanceof Player)) return Result.ONLY_PLAYER;

        if (args.length() < 4) return Result.WRONG_USAGE;

        String targetPlayerName = args.get(1);
        Player targetPlayer = Bukkit.getPlayer(targetPlayerName);
        if (targetPlayer == null || !targetPlayer.isOnline()) {
            notifier.announce(message.get(getSender(), "event.player_not_found"));
            return Result.FAILURE;
        }

        String amountStr = args.get(2);
        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            notifier.announce(message.get(getSender(), "event.invalid_amount"));
            return Result.FAILURE;
        }

        String donationMessage = String.join(" ", Arrays.copyOfRange(args.args(), 3, args.args().length));

        Donation donation = new Donation(
                targetPlayer.getUniqueId(),
                Services.Chzzk,
                Platform.CHZZK,
                DonationType.CHAT,
                targetPlayer.getName(),
                "DonationAPI",
                "DonationAPI",
                donationMessage,
                amount
        );

        DonationEvent event = new DonationEvent(targetPlayer, donation);
        Bukkit.getPluginManager().callEvent(event);

        notifier.announce(message.get(getSender(), "event.success")
                .replace("{player}", targetPlayer.getName()));
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
