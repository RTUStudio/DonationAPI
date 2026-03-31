package kr.rtustudio.donation.service.soop.live;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import kr.rtustudio.donation.common.data.LiveStatus;
import kr.rtustudio.donation.common.live.LiveStatusChecker;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "DonationAPI/SOOP")
public class SoopLiveChecker implements LiveStatusChecker {

    private static final String LIVE_API_URL = "https://live.sooplive.com/afreeca/player_live_api.php?bjid=%s";
    private static final String CHANNEL_URL = "https://play.sooplive.com/%s";
    private static final MediaType FORM = MediaType.get("application/x-www-form-urlencoded");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    @Override
    public CompletableFuture<LiveStatus> checkLive(String bjId) {
        return CompletableFuture.supplyAsync(() -> {
            String url = String.format(CHANNEL_URL, bjId);
            try {
                String formBody = "bid=" + bjId + "&type=live&confirm_allow_with_adult_cpn=n&player_type=html5";
                Request request = new Request.Builder()
                        .url(String.format(LIVE_API_URL, bjId))
                        .post(RequestBody.create(formBody, FORM))
                        .build();

                try (Response response = httpClient.newCall(request).execute()) {
                    if (!response.isSuccessful()) return LiveStatus.offline(url);

                    String body = response.body() != null ? response.body().string() : "";
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    JsonObject channel = json.has("CHANNEL") ? json.getAsJsonObject("CHANNEL") : null;

                    if (channel != null && channel.has("RESULT") && channel.get("RESULT").getAsInt() == 1) {
                        String title = channel.has("TITLE") ? channel.get("TITLE").getAsString() : null;
                        int viewers = channel.has("CNTUSER") ? channel.get("CNTUSER").getAsInt() : 0;
                        return LiveStatus.online(title, viewers, url);
                    }

                    return LiveStatus.offline(url);
                }
            } catch (Exception e) {
                log.warn("Failed to check SOOP live status for {}: {}", bjId, e.getMessage());
                return LiveStatus.offline(url);
            }
        });
    }
}
