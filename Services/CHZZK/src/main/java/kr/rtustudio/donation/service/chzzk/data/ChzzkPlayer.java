package kr.rtustudio.donation.service.chzzk.data;

import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.data.UserData;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 치지직 플레이어 데이터
 *
 * @param uuid      플레이어 UUID
 * @param channelId 채널 고유 ID
 * @param token     OAuth 토큰
 */
public record ChzzkPlayer(UUID uuid, String channelId, ChzzkToken token) implements UserData {

    @Override
    public Platform platform() {
        return Platform.CHZZK;
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
