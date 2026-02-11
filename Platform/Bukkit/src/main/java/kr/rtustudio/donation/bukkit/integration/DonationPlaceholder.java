package kr.rtustudio.donation.bukkit.integration;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.DonationPlayerManager;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.framework.bukkit.api.integration.wrapper.PlaceholderWrapper;
import org.bukkit.OfflinePlayer;

/**
 * PlaceholderAPI 연동
 * <p>
 * 플레이어의 플랫폼 연결 상태를 PlaceholderAPI를 통해 제공합니다.
 *
 * <p>사용 가능한 플레이스홀더:
 * <ul>
 *   <li>%donationapi_chzzk_official% - 치지직 공식 연결 상태</li>
 *   <li>%donationapi_chzzk_unofficial% - 치지직 비공식 연결 상태</li>
 *   <li>%donationapi_ssapi_chzzk% - SSAPI 치지직 연결 상태</li>
 *   <li>%donationapi_ssapi_soop% - SSAPI 숲 연결 상태</li>
 *   <li>%donationapi_soop% - 숲 연결 상태</li>
 *   <li>%donationapi_youtube% - 유튜브 연결 상태</li>
 *   <li>%donationapi_toonation% - 투네이션 연결 상태</li>
 * </ul>
 */
public class DonationPlaceholder extends PlaceholderWrapper<BukkitDonationAPI> {

    private final DonationPlayerManager playerManager;

    public DonationPlaceholder(BukkitDonationAPI plugin) {
        super(plugin);
        this.playerManager = plugin.getPlayerManager();
    }

    @Override
    public String onRequest(OfflinePlayer player, String[] params) {
        if (params.length == 0) return "";
        if (player == null) return "";

        String key = params[0].toLowerCase();

        if (params.length >= 2) {
            String type = params[1].toLowerCase();
            return switch (key) {
                case "chzzk" -> switch (type) {
                    case "official" ->
                            String.valueOf(playerManager.isActive(player.getUniqueId(), Services.ChzzkOfficial));
                    case "unofficial" ->
                            String.valueOf(playerManager.isActive(player.getUniqueId(), Services.ChzzkUnofficial));
                    default -> "";
                };
                case "ssapi" -> switch (type) {
                    case "chzzk" ->
                            String.valueOf(playerManager.isActive(player.getUniqueId(), Services.SSAPI, Platform.CHZZK));
                    case "soop" ->
                            String.valueOf(playerManager.isActive(player.getUniqueId(), Services.SSAPI, Platform.SOOP));
                    default -> "";
                };
                default -> "";
            };
        }

        return switch (key) {
            case "soop" -> String.valueOf(playerManager.isActive(player.getUniqueId(), Services.SOOP));
            case "toonation" -> String.valueOf(playerManager.isActive(player.getUniqueId(), Services.Toonation));
            case "youtube" -> String.valueOf(playerManager.isActive(player.getUniqueId(), Services.Youtube));
            default -> "";
        };
    }
}
