package kr.rtustudio.donation.bukkit.event;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Getter
@ToString
@RequiredArgsConstructor
public class DonationEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();

    @Nullable
    private final Player player;
    private final Donation donation;

    @Setter
    private boolean isCancelled = false;

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Services getService() {
        return donation.service();
    }

    public Platform getPlatform() {
        return donation.platform();
    }

    public String getStreamer() {
        return donation.streamer();
    }

    public String getDonator() {
        return donation.donator();
    }

    public String getNickname() {
        return donation.nickname();
    }

    public String getMessage() {
        return donation.message();
    }

    public int getAmount() {
        return donation.amount();
    }

    public String getUnit() {
        return donation.unit();
    }

    public int getPrice() {
        return donation.price();
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

}
