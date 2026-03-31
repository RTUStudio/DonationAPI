package kr.rtustudio.donation.service.cime.live;

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

@Slf4j(topic = "DonationAPI/CIME")
public class CimeLiveChecker implements LiveStatusChecker {

    private static final String LIVE_API_URL = "https://prod.ci.me/api/app/channels/%s/live";
    private static final String CHANNEL_URL = "https://ci.me/%s";

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    public CompletableFuture<LiveStatus> checkLive(String channelSlug) {
        return CompletableFuture.supplyAsync(() -> {
            String url = String.format(CHANNEL_URL, channelSlug);
            try {
                Request request = new Request.Builder()
                        .url(String.format(LIVE_API_URL, channelSlug))
                        .get()
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (response.code() == 404) return LiveStatus.offline(url);
                    if (!response.isSuccessful()) return LiveStatus.offline(url);

                    String body = response.body() != null ? response.body().string() : "";
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    String title = json.has("title") ? json.get("title").getAsString() : null;
                    int viewCount = json.has("viewCount") ? json.get("viewCount").getAsInt() : 0;

                    return LiveStatus.online(title, viewCount, url);
                }
            } catch (Exception e) {
                log.warn("Failed to check CIME live status for {}: {}", channelSlug, e.getMessage());
                return LiveStatus.offline(url);
            }
        });
    }
}
