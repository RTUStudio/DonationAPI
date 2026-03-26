package kr.rtustudio.donation.service.youtube.live;

import kr.rtustudio.donation.common.data.LiveStatus;
import kr.rtustudio.donation.common.live.LiveStatusChecker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * YouTube 라이브 상태 확인 (RSS 전용, 경량)
 * <p>
 * HTML 스크래핑 없이 RSS 피드만 사용합니다.
 * 제목은 RSS에서 추출하며, 라이브 여부와 시청자 수는 RSS로 판별 불가능하므로
 * 실제 라이브 판별은 기존 YoutubeClient(후원 폴링)의 연결 상태에 위임합니다.
 */
@Slf4j(topic = "DonationAPI/Youtube")
public class YoutubeLiveChecker implements LiveStatusChecker {

    private static final String RSS_URL = "https://www.youtube.com/feeds/videos.xml?user=%s";
    private static final String CHANNEL_URL = "https://www.youtube.com/@%s/live";
    private static final Pattern TITLE_PATTERN = Pattern.compile("<entry>.*?<title>([^<]+)</title>", Pattern.DOTALL);

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    public CompletableFuture<LiveStatus> checkLive(String handle) {
        return CompletableFuture.supplyAsync(() -> {
            String cleanHandle = handle.startsWith("@") ? handle.substring(1) : handle;
            String url = String.format(CHANNEL_URL, cleanHandle);
            try {
                String rssBody = fetchRss(cleanHandle);
                if (rssBody == null) return LiveStatus.offline(url);

                String title = extractFirst(TITLE_PATTERN, rssBody);

                // RSS만으로는 라이브 여부를 판별할 수 없으므로 제목/URL만 캐싱
                // 실제 라이브 판별은 YoutubeClient(후원 폴링) 연결 상태로 대체
                return new LiveStatus(false, title, 0, url, System.currentTimeMillis());
            } catch (Exception e) {
                log.warn("Failed to fetch YouTube RSS for {}: {}", handle, e.getMessage());
                return LiveStatus.offline(url);
            }
        });
    }

    private String fetchRss(String username) throws Exception {
        Request request = new Request.Builder()
                .url(String.format(RSS_URL, username))
                .get()
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) return null;
            return response.body() != null ? response.body().string() : null;
        }
    }

    private String extractFirst(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        return matcher.find() ? matcher.group(1) : null;
    }
}
