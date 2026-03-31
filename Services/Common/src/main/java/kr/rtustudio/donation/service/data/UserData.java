package kr.rtustudio.donation.service.data;

import kr.rtustudio.donation.common.Platform;

import java.util.UUID;

/**
 * 사용자 데이터 인터페이스
 * <p>
 * 모든 플랫폼 데이터 클래스가 구현해야 하는 기본 인터페이스입니다.
 * UUID를 통해 플레이어를 식별합니다.
 */
public interface UserData {

    /**
     * 유저의 플레이어 UUID를 반환합니다.
     *
     * @return 플레이어 UUID
     */
    UUID uuid();

    /**
     * 유저의 플랫폼을 반환합니다
     *
     * @return 유저 플랫폼
     */
    Platform platform();

    /**
     * 스트리머 식별자를 반환합니다.
     *
     * @return 스트리머 ID
     */
    String channelId();

    /**
     * 스트리머의 채널 이름이나 닉네임을 반환합니다.
     *
     * @return 닉네임. 지원하지 않을 경우 식별자와 동일함.
     */
    default String channelName() {
        return channelId();
    }
}
