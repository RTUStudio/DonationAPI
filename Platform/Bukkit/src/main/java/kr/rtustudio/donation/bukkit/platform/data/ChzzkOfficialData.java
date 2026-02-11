package kr.rtustudio.donation.bukkit.platform.data;

import kr.rtustudio.donation.common.Platform;

import java.util.UUID;

/**
 * 치지직 공식 연결 데이터
 * <p>
 * 치지직 공식 API의 연결 정보를 저장합니다.
 */
public record ChzzkOfficialData(
        UUID uuid,
        String channelId,
        String accessToken,
        String refreshToken
) implements UserData {

    @Override
    public Platform platform() {
        return Platform.CHZZK;
    }
}
