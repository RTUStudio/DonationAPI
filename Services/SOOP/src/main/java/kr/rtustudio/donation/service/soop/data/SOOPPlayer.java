package kr.rtustudio.donation.service.soop.data;

import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.data.UserData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record SOOPPlayer(UUID uuid, String channelId, SOOPToken token) implements UserData {

    @Override
    public Platform platform() {
        return Platform.SOOP;
    }

    @NotNull
    public String accessToken() {
        return token.accessToken();
    }

    @NotNull
    public String refreshToken() {
        return token.refreshToken();
    }
}
