package kr.rtustudio.donation.service.chzzk.live;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.rtustudio.donation.common.data.LiveStatus;
import kr.rtustudio.donation.common.live.LiveStatusChecker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "DonationAPI/Chzzk")
public class ChzzkLiveChecker implements LiveStatusChecker {

    private static final String LIVE_API_URL = "https://api.chzzk.naver.com/polling/v2/channels/%s/live-status";
    private static final String CHANNEL_URL = "https://chzzk.naver.com/live/%s";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    public CompletableFuture<LiveStatus> checkLive(String channelId) {
        return CompletableFuture.supplyAsync(() -> {
            String url = String.format(CHANNEL_URL, channelId);
            try {
                Request request = new Request.Builder()
                        .url(String.format(LIVE_API_URL, channelId))
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) return LiveStatus.offline(url);

                    String body = response.body() != null ? response.body().string() : "";
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    JsonObject content = json.has("content") ? json.getAsJsonObject("content") : null;

                    if (content != null && content.has("status") && "OPEN".equals(content.get("status").getAsString())) {
                        String title = content.has("liveTitle") ? content.get("liveTitle").getAsString() : null;
                        int viewers = content.has("concurrentUserCount") ? content.get("concurrentUserCount").getAsInt() : 0;
                        return LiveStatus.online(title, viewers, url);
                    }

                    return LiveStatus.offline(url);
                }
            } catch (Exception e) {
                log.warn("Failed to check CHZZK live status for {}: {}", channelId, e.getMessage());
                return LiveStatus.offline(url);
            }
        });
    }
}
