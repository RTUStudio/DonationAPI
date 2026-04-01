package kr.rtustudio.donation.bukkit.integration;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.DonationManager;
import kr.rtustudio.donation.bukkit.manager.LiveStatusManager;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.common.data.LiveStatus;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.integration.wrapper.PlaceholderArgs;
import kr.rtustudio.framework.bukkit.api.integration.wrapper.PlaceholderWrapper;
import org.bukkit.OfflinePlayer;

/**
 * PlaceholderAPI 연동
 * <p>
 * 플레이어의 플랫폼 연결 상태 및 라이브 방송 상태를 PlaceholderAPI를 통해 제공합니다.
 *
 * <p>사용 가능한 플레이스홀더:
 * <ul>
 *   <li>%donationapi_chzzk% - 치지직 연결 상태</li>
 *   <li>%donationapi_ssapi_chzzk% - SSAPI 치지직 연결 상태</li>
 *   <li>%donationapi_ssapi_soop% - SSAPI 숲 연결 상태</li>
 *   <li>%donationapi_soop% - 숲 연결 상태</li>
 *   <li>%donationapi_youtube% - 유튜브 연결 상태</li>
 *   <li>%donationapi_toonation% - 투네이션 연결 상태</li>
 *   <li>%donationapi_live_cime% - 씨미 라이브 여부</li>
 *   <li>%donationapi_live_soop% - 숲 라이브 여부</li>
 *   <li>%donationapi_live_chzzk% - 치지직 라이브 여부</li>
 *   <li>%donationapi_live_youtube% - 유튜브 라이브 여부</li>
 *   <li>%donationapi_title_cime% - 씨미 방 제목</li>
 *   <li>%donationapi_title_soop% - 숲 방 제목</li>
 *   <li>%donationapi_title_chzzk% - 치지직 방 제목</li>
 *   <li>%donationapi_viewers_soop% - 숲 시청자 수</li>
 *   <li>%donationapi_viewers_chzzk% - 치지직 시청자 수</li>
 *   <li>%donationapi_url_cime% - 씨미 채널 URL</li>
 *   <li>%donationapi_url_soop% - 숲 채널 URL</li>
 *   <li>%donationapi_url_chzzk% - 치지직 채널 URL</li>
 *   <li>%donationapi_url_youtube% - 유튜브 채널 URL</li>
 * </ul>
 */
public class DonationPlaceholder extends PlaceholderWrapper<BukkitDonationAPI> {

    private final DonationManager donationManager;
    private final LiveStatusManager liveStatusManager;

    public DonationPlaceholder(BukkitDonationAPI plugin) {
        super(plugin);
        this.donationManager = plugin.getDonationManager();
        this.liveStatusManager = plugin.getLiveStatusManager();
    }

    @Override
    public String onRequest(OfflinePlayer player, PlaceholderArgs args) {
        String[] params = args.args();
        if (params.length == 0) return "";
        if (player == null) return "";

        String key = params[0].toLowerCase();

        // 2-arg: ssapi_chzzk, ssapi_soop, live_*, title_*, viewers_*, url_*
        if (params.length >= 2) {
            String type = params[1].toLowerCase();
            return switch (key) {
                case "ssapi" -> switch (type) {
                    case "chzzk" ->
                            String.valueOf(donationManager.isActive(player.getUniqueId(), Services.SSAPI, Platform.CHZZK));
                    case "soop" ->
                            String.valueOf(donationManager.isActive(player.getUniqueId(), Services.SSAPI, Platform.SOOP));
                    default -> "";
                };
                case "live" -> {
                    Services service = Services.from(type);
                    if (service == null) yield "";
                    LiveStatus status = liveStatusManager.getLiveStatus(service, player.getUniqueId());
                    yield status != null ? String.valueOf(status.live()) : "false";
                }
                case "title" -> {
                    Services service = Services.from(type);
                    if (service == null) yield "";
                    LiveStatus status = liveStatusManager.getLiveStatus(service, player.getUniqueId());
                    yield status != null && status.title() != null ? status.title() : "";
                }
                case "viewers" -> {
                    Services service = Services.from(type);
                    if (service == null) yield "";
                    LiveStatus status = liveStatusManager.getLiveStatus(service, player.getUniqueId());
                    yield status != null ? String.valueOf(status.viewerCount()) : "0";
                }
                case "url" -> {
                    Services service = Services.from(type);
                    if (service == null) yield "";
                    LiveStatus status = liveStatusManager.getLiveStatus(service, player.getUniqueId());
                    yield status != null ? status.channelUrl() : "";
                }
                default -> "";
            };
        }

        // 1-arg: 연결 상태
        Services service = Services.from(key);
        if (service != null) {
            return String.valueOf(donationManager.isActive(player.getUniqueId(), service));
        }
        return "";
    }
}
