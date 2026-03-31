package kr.rtustudio.donation.service.toonation.data;

import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.data.UserData;

import java.util.UUID;

/**
 * 투네이션 플레이어 데이터
 *
 * @param uuid      플레이어 UUID
 * @param alertKey  알림 키 (투네이션 위젯 주소의 끝부분)
 * @param payload   WebSocket 통신용 payload
 */
public record ToonationPlayer(
        UUID uuid,
        String alertKey,
        String payload
) implements UserData {

    @Override
    public String channelId() {
        return alertKey;
    }

    @Override
    public Platform platform() {
        return Platform.TOONATION;
    }
}
