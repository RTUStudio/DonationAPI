package kr.rtustudio.donation.service.soop.net.socket;

import kr.rtustudio.donation.service.soop.data.SoopDonationMessage;
import kr.rtustudio.donation.service.soop.data.SoopDonationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoopMessageParser {

    public record ParsedMessage(@NotNull String action, @Nullable Object message) {}

    public static @Nullable ParsedMessage parse(int serviceCode, @NotNull String[] f) {
        return switch (serviceCode) {
            case SoopServiceCode.SVC_LOGIN, SoopServiceCode.SVC_SDK_LOGIN -> new ParsedMessage("LOGIN", null);
            case SoopServiceCode.SVC_JOINCH, SoopServiceCode.SVC_FREECAT_OWNER_JOIN -> new ParsedMessage("JOIN", null);

            // SDK: bjId=t[0], userId=t[1], userNickname=t[2], count=t[3], fanNumber=t[4]
            case SoopServiceCode.SVC_SENDBALLOON -> donation("BALLOON_GIFTED", SoopDonationType.BALLOON_GIFTED,
                    get(f, 0), get(f, 1), get(f, 2), num(f, 3), null);

            // TODO: 필요 시 활성화
            // case SoopServiceCode.SVC_SENDBALLOONSUB -> donation("BALLOON_GIFTED", SoopDonationType.BALLOON_GIFTED, get(f, 1), get(f, 3), get(f, 4), num(f, 5), null);
            // case SoopServiceCode.SVC_VODBALLOON -> donation("BALLOON_GIFTED", SoopDonationType.BALLOON_GIFTED, get(f, 0), get(f, 1), get(f, 2), num(f, 3), null);
            // case SoopServiceCode.SVC_VIDEO_BALLOON -> donation("BALLOON_GIFTED", SoopDonationType.BALLOON_GIFTED, get(f, 1), get(f, 2), get(f, 3), num(f, 4), null);
            // case SoopServiceCode.SVC_SENDFANLETTER -> donation("STICKER_GIFTED", SoopDonationType.STICKER_GIFTED, get(f, 0), get(f, 2), get(f, 3), num(f, 7), null);
            // case SoopServiceCode.SVC_SENDFANLETTERSUB -> donation("STICKER_GIFTED", SoopDonationType.STICKER_GIFTED, get(f, 1), get(f, 3), get(f, 4), num(f, 8), null);
            // case SoopServiceCode.SVC_ADCON_EFFECT -> donation("ADBALLOON_GIFTED", SoopDonationType.AD_BALLOON_GIFTED, get(f, 1), get(f, 2), get(f, 3), num(f, 9), null);
            // case SoopServiceCode.SVC_STATION_ADCON -> donation("ADBALLOON_GIFTED", SoopDonationType.AD_BALLOON_GIFTED, get(f, 0), get(f, 1), get(f, 2), num(f, 3), null);
            // case SoopServiceCode.SVC_SENDQUICKVIEW -> donation("QUICKVIEW_GIFTED", SoopDonationType.QUICKVIEW_GIFTED, null, get(f, 1), get(f, 2), 1, null);
            // case SoopServiceCode.SVC_FOLLOW_ITEM -> donation("SUBSCRIBED", SoopDonationType.SUBSCRIBED, get(f, 1), get(f, 2), get(f, 3), 1, null);
            // case SoopServiceCode.SVC_FOLLOW_ITEM_EFFECT -> donation("SUBSCRIPTION_RENEWED", SoopDonationType.SUBSCRIPTION_RENEWED, get(f, 0), get(f, 1), get(f, 2), num(f, 3), null);
            // case SoopServiceCode.SVC_SENDSUBSCRIPTION -> donation("SUBSCRIPTION_GIFTED", SoopDonationType.SUBSCRIBED, get(f, 5), get(f, 1), get(f, 2), 1, null);
            // case SoopServiceCode.SVC_OGQ_EMOTICON_GIFT -> donation("OGQ_EMOTICON_GIFTED", SoopDonationType.STICKER_GIFTED, null, get(f, 1), get(f, 2), 1, get(f, 5));
            // case SoopServiceCode.SVC_GEM_ITEMSEND -> donation("GEM_GIFTED", SoopDonationType.BALLOON_GIFTED, null, get(f, 1), get(f, 2), 1, get(f, 3));

            default -> null;
        };
    }

    private static @NotNull ParsedMessage donation(
            @NotNull String action, @NotNull SoopDonationType type,
            @Nullable String bjId, @Nullable String userId, @Nullable String userNickname,
            int count, @Nullable String message) {
        return new ParsedMessage(action, new SoopDonationMessage(type, bjId, userId, userNickname, count, message));
    }

    private static @Nullable String get(@NotNull String[] arr, int index) {
        return index < arr.length ? arr[index] : null;
    }

    private static int num(@NotNull String[] arr, int index) {
        if (index >= arr.length || arr[index] == null || arr[index].isEmpty()) return 0;
        try {
            return Integer.parseInt(arr[index]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

}
