package kr.rtustudio.donation.bukkit.entity;

import kr.rtustudio.donation.bukkit.component.PlatformStatusComponent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Getter
@RequiredArgsConstructor
public class DonationEntity {

    @NotNull
    private final UUID uuid;
    private final PlatformStatusComponent platformStatus = new PlatformStatusComponent();

    public DonationEntity(@NotNull UUID uuid, PlatformStatusComponent platformStatus) {
        this.uuid = uuid;
        this.platformStatus.getStatuses().putAll(platformStatus.getStatuses());
    }

    @Nullable
    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public boolean isOnline() {
        Player player = getPlayer();
        return player != null && player.isOnline();
    }
}
