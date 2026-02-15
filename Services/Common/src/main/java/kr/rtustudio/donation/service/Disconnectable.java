package kr.rtustudio.donation.service;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 플레이어 단위 연결 해제를 지원하는 서비스 인터페이스
 */
public interface Disconnectable {

    /**
     * 특정 플레이어의 서비스 연결을 해제합니다.
     *
     * @param uuid 플레이어 UUID
     */
    void disconnect(@NotNull UUID uuid);
}
