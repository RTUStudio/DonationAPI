package kr.rtustudio.donation.bukkit.platform.data;

import kr.rtustudio.donation.common.Platform;

import java.util.UUID;

/**
 * SSAPI 연결 데이터
 * <p>
 * SSAPI 서비스의 연결 정보를 저장합니다.
 */
public record SSAPIData(
        UUID uuid,
        String streamerId,
        Platform platform
) implements UserData {
}
