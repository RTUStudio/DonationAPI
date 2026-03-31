package kr.rtustudio.donation.service.cime.data;

import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.data.UserData;

import java.util.UUID;

/**
 * Cime 플레이어 데이터
 *
 * @param uuid        플레이어 UUID
 * @param alertKey    후원 알림 키 (UUID 형식)
 * @param channelSlug 채널 슬러그 (라이브 상태 확인 및 URL 생성용)
 */
public record CimePlayer(
        UUID uuid,
        String alertKey,
        String channelSlug
) implements UserData {

    @Override
    public String channelId() {
        return channelSlug;
    }

    @Override
    public Platform platform() {
        return Platform.CIME;
    }
}
