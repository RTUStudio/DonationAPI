package kr.rtustudio.donation.service.chzzk.official.data;

import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.data.UserData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record ChzzkPlayer(UUID uuid, String channelId, ChzzkToken token) implements UserData {

    @Override
    public Platform platform() {
        return Platform.CHZZK;
    }

    @Override
    public String streamerId() {
        return channelId;
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
