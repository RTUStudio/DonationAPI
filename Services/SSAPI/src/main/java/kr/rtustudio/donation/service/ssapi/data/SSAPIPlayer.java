package kr.rtustudio.donation.service.ssapi.data;

import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.data.UserData;

import java.util.UUID;

/**
 * SSAPI 연결 데이터
 * <p>
 * SSAPI 서비스의 연결 정보를 저장합니다.
 */
public record SSAPIPlayer(
        UUID uuid,
        String channelId,
        Platform platform
) implements UserData {
}
