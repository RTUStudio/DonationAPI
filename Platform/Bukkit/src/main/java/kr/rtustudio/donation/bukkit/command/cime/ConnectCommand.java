package kr.rtustudio.donation.bukkit.command.cime;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.PlatformConnectionManager;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.cime.CimeService;
import kr.rtustudio.donation.service.cime.data.CimePlayer;
import kr.rtustudio.framework.bukkit.api.command.CommandArgs;
import kr.rtustudio.framework.bukkit.api.command.RSCommand;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Cime 연동 명령어
 * <p>
 * 사용법: /후원API cime 연동 <alertKey 또는 알람 링크>
 */
public class ConnectCommand extends RSCommand<BukkitDonationAPI> {

    private static final Pattern CHANNEL_SLUG_PATTERN = Pattern.compile("\"channelSlug\":\"([^\"]+)\"");
    private static final Pattern CHANNEL_ID_PATTERN = Pattern.compile("\"channelId\":\"([^\"]+)\"");

    private final PlatformConnectionManager connectionManager;
    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();

    public ConnectCommand(BukkitDonationAPI plugin) {
        super(plugin, "connect");
        this.connectionManager = plugin.getConnectionManager();
    }

    @Override
    protected Result execute(CommandArgs args) {
        if (!(getSender() instanceof Player player)) return Result.ONLY_PLAYER;

        if (!args.length(3)) {
            notifier.announce(message.get(player, "cime.usage"));
            return Result.FAILURE;
        }

        String input = args.get(2);

        // 이미 연결된 경우 기존 연결 해제
        connectionManager.disconnect(player.getUniqueId(), Services.Cime);

        CimeService cimeService = plugin.getDonationAPI().get(Services.Cime, CimeService.class);
        if (cimeService == null) {
            notifier.announce(message.get(player, "service.disabled"));
            return Result.FAILURE;
        }

        // 링크 형식이면 HTML 파싱으로 channelSlug 추출
        if (input.startsWith("http")) {
            notifier.announce(message.get(player, "connect.connecting"));
            CompletableFuture.supplyAsync(() -> parseAlertLink(input)).thenAccept(parsed -> {
                if (parsed == null) {
                    notifier.announce(player, message.get(player, "connect.fail"));
                    return;
                }
                CimePlayer data = new CimePlayer(player.getUniqueId(), parsed[0], parsed[1], parsed[2]);
                boolean success = cimeService.reconnect(player.getUniqueId(), data);
                if (success) {
                    notifier.announce(player, message.get(player, "connect.success")
                            .replace("{service}", "Cime")
                            .replace("{platform}", "CIME")
                            .replace("{id}", parsed[2]));
                } else {
                    notifier.announce(player, message.get(player, "connect.fail"));
                }
            });
            return Result.SUCCESS;
        }

        // 단순 alertKey 입력
        String key = input;
        if (input.contains("/")) {
            key = input.substring(input.lastIndexOf('/') + 1);
            if (key.contains("?")) key = key.substring(0, key.indexOf('?'));
        }

        CimePlayer data = new CimePlayer(player.getUniqueId(), key, key, key);
        boolean success = cimeService.reconnect(player.getUniqueId(), data);

        if (success) {
            notifier.announce(message.get(player, "connect.success")
                    .replace("{service}", "Cime")
                    .replace("{platform}", "CIME")
                    .replace("{id}", key.substring(0, Math.min(8, key.length())) + "..."));
            return Result.SUCCESS;
        }

        notifier.announce(message.get(player, "connect.fail"));
        return Result.FAILURE;
    }

    /**
     * 알람 링크의 HTML에서 channelId, alertKey, channelSlug를 파싱합니다.
     *
     * @param url 씨미 알람 링크
     * @return [channelId, alertKey, channelSlug] 또는 null
     */
    private String[] parseAlertLink(String url) {
        try {
            // URL에서 alertKey 추출 (마지막 경로 세그먼트)
            String alertKey = url.substring(url.lastIndexOf('/') + 1);
            if (alertKey.contains("?")) alertKey = alertKey.substring(0, alertKey.indexOf('?'));

            Request request = new Request.Builder().url(url).get().build();
            try (Response response = httpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) return null;
                String body = response.body() != null ? response.body().string() : "";

                String channelSlug = null;
                String channelId = null;

                Matcher slugMatcher = CHANNEL_SLUG_PATTERN.matcher(body);
                if (slugMatcher.find()) channelSlug = slugMatcher.group(1);

                Matcher idMatcher = CHANNEL_ID_PATTERN.matcher(body);
                if (idMatcher.find()) channelId = idMatcher.group(1);

                if (channelSlug == null) return null;
                if (channelId == null) channelId = alertKey;

                return new String[]{channelId, alertKey, channelSlug};
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<String> tabComplete(CommandArgs args) {
        if (args.length(3)) return List.of("<url>");
        return List.of();
    }
}
