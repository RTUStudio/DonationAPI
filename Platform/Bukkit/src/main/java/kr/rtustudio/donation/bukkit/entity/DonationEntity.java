package kr.rtustudio.donation.bukkit.entity;

import kr.rtustudio.donation.bukkit.component.PlatformStatusComponent;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
public class DonationEntity {

    @NotNull
    private final UUID uuid;
    @NotNull
    private final PlatformStatusComponent platformStatus;

    public DonationEntity(@NotNull UUID uuid) {
        this(uuid, new PlatformStatusComponent());
    }

    public DonationEntity(@NotNull UUID uuid, @NotNull PlatformStatusComponent platformStatus) {
        this.uuid = uuid;
        this.platformStatus = platformStatus;
    }
}
