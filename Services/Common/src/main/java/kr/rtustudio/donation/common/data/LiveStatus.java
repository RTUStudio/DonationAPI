package kr.rtustudio.donation.common.data;

import org.jetbrains.annotations.Nullable;

/**
 * 캐싱된 라이브 방송 상태
 *
 * @param live        방송 중 여부
 * @param title       방 제목 (nullable)
 * @param viewerCount 시청자 수
 * @param channelUrl  방송 채널 URL
 * @param updatedAt   캐시 갱신 시각 (System.currentTimeMillis)
 */
public record LiveStatus(
        boolean live,
        @Nullable String title,
        int viewerCount,
        String channelUrl,
        long updatedAt
) {

    public static LiveStatus offline(String channelUrl) {
        return new LiveStatus(false, null, 0, channelUrl, System.currentTimeMillis());
    }

    public static LiveStatus online(String title, int viewerCount, String channelUrl) {
        return new LiveStatus(true, title, viewerCount, channelUrl, System.currentTimeMillis());
    }
}
