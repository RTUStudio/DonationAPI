package kr.rtustudio.donation.bukkit.event;

import kr.rtustudio.donation.common.data.Donation;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

@Getter
@ToString
public class DonationEvent extends Event implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final String platform;
    private final String streamerId;
    private final String userId;
    private final String nickname;
    private final int count;
    private final String message;
    private final int amount;
    private final Map<String, Object> extras;
    @Setter
    private boolean isCancelled = false;

    public DonationEvent(Donation donation) {
        this.platform = donation.platform();
        this.streamerId = donation.streamerId();
        this.userId = donation.userId();
        this.nickname = donation.nickname();
        this.count = donation.count();
        this.message = donation.message();
        this.amount = donation.amount();
        this.extras = donation.extras();
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public @NotNull HandlerList getHandlers() {
        return handlers;
    }

}
