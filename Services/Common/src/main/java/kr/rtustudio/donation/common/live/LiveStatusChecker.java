package kr.rtustudio.donation.common.live;

import kr.rtustudio.donation.common.data.LiveStatus;

import java.util.concurrent.CompletableFuture;

/**
 * 라이브 상태 확인 인터페이스
 * <p>
 * 각 플랫폼 서비스 모듈에서 구현하여 라이브 방송 여부를 확인합니다.
 */
public interface LiveStatusChecker {

    /**
     * 채널의 라이브 상태를 비동기로 확인합니다.
     *
     * @param channelIdentifier 플랫폼별 채널 식별자
     * @return 라이브 상태
     */
    CompletableFuture<LiveStatus> checkLive(String channelIdentifier);
}
