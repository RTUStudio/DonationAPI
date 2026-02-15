package kr.rtustudio.donation.bukkit.integration;

import kr.rtustudio.donation.bukkit.BukkitDonationAPI;
import kr.rtustudio.donation.bukkit.manager.DonationManager;
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
 *   <li>%donationapi_chzzk% - 치지직 연결 상태</li>
 *   <li>%donationapi_ssapi_chzzk% - SSAPI 치지직 연결 상태</li>
 *   <li>%donationapi_ssapi_soop% - SSAPI 숲 연결 상태</li>
 *   <li>%donationapi_soop% - 숲 연결 상태</li>
 *   <li>%donationapi_youtube% - 유튜브 연결 상태</li>
 *   <li>%donationapi_toonation% - 투네이션 연결 상태</li>
 * </ul>
 */
public class DonationPlaceholder extends PlaceholderWrapper<BukkitDonationAPI> {

    private final DonationManager donationManager;

    public DonationPlaceholder(BukkitDonationAPI plugin) {
        super(plugin);
        this.donationManager = plugin.getDonationManager();
    }

    @Override
    public String onRequest(OfflinePlayer player, String[] params) {
        if (params.length == 0) return "";
        if (player == null) return "";

        String key = params[0].toLowerCase();

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
                default -> "";
            };
        }

        return switch (key) {
            case "chzzk" -> String.valueOf(donationManager.isActive(player.getUniqueId(), Services.Chzzk));
            case "soop" -> String.valueOf(donationManager.isActive(player.getUniqueId(), Services.SOOP));
            case "toonation" -> String.valueOf(donationManager.isActive(player.getUniqueId(), Services.Toonation));
            case "youtube" -> String.valueOf(donationManager.isActive(player.getUniqueId(), Services.Youtube));
            default -> "";
        };
    }
}
