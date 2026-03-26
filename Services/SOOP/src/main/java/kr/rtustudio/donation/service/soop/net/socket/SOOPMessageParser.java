package kr.rtustudio.donation.service.soop.net.socket;

import kr.rtustudio.donation.service.soop.data.SOOPDonationMessage;
import kr.rtustudio.donation.service.soop.data.SOOPDonationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SOOPMessageParser {

    public record ParsedMessage(@NotNull String action, @Nullable Object message) {}

    public static @Nullable ParsedMessage parse(int serviceCode, @NotNull String[] f) {
        return switch (serviceCode) {
            case SOOPServiceCode.SVC_LOGIN, SOOPServiceCode.SVC_SDK_LOGIN -> new ParsedMessage("LOGIN", null);
            case SOOPServiceCode.SVC_JOINCH, SOOPServiceCode.SVC_FREECAT_OWNER_JOIN -> new ParsedMessage("JOIN", null);

            // SDK: bjId=t[0], userId=t[1], userNickname=t[2], count=t[3], fanNumber=t[4]
            case SOOPServiceCode.SVC_SENDBALLOON -> donation("BALLOON_GIFTED", SOOPDonationType.BALLOON_GIFTED,
                    get(f, 0), get(f, 1), get(f, 2), num(f, 3), null);

            // TODO: 필요 시 활성화
            // case SOOPServiceCode.SVC_SENDBALLOONSUB -> donation("BALLOON_GIFTED", SOOPDonationType.BALLOON_GIFTED, get(f, 1), get(f, 3), get(f, 4), num(f, 5), null);
            // case SOOPServiceCode.SVC_VODBALLOON -> donation("BALLOON_GIFTED", SOOPDonationType.BALLOON_GIFTED, get(f, 0), get(f, 1), get(f, 2), num(f, 3), null);
            // case SOOPServiceCode.SVC_VIDEO_BALLOON -> donation("VIDEOBALLOON_GIFTED", SOOPDonationType.BALLOON_GIFTED, get(f, 1), get(f, 2), get(f, 3), num(f, 4), null);
            // case SOOPServiceCode.SVC_SENDFANLETTER -> donation("STICKER_GIFTED", SOOPDonationType.STICKER_GIFTED, get(f, 0), get(f, 2), get(f, 3), num(f, 7), null);
            // case SOOPServiceCode.SVC_SENDFANLETTERSUB -> donation("STICKER_GIFTED", SOOPDonationType.STICKER_GIFTED, get(f, 1), get(f, 3), get(f, 4), num(f, 8), null);
            case SOOPServiceCode.SVC_ADCON_EFFECT -> donation("ADBALLOON_GIFTED", SOOPDonationType.AD_BALLOON_GIFTED, get(f, 1), get(f, 2), get(f, 3), num(f, 9), null);
            // case SOOPServiceCode.SVC_STATION_ADCON -> donation("ADBALLOON_GIFTED", SOOPDonationType.AD_BALLOON_GIFTED, get(f, 0), get(f, 1), get(f, 2), num(f, 3), null);
            // case SOOPServiceCode.SVC_SENDQUICKVIEW -> donation("QUICKVIEW_GIFTED", SOOPDonationType.QUICKVIEW_GIFTED, null, get(f, 1), get(f, 2), 1, null);
            // case SOOPServiceCode.SVC_FOLLOW_ITEM -> donation("SUBSCRIBED", SOOPDonationType.SUBSCRIBED, get(f, 1), get(f, 2), get(f, 3), 1, null);
            // case SOOPServiceCode.SVC_FOLLOW_ITEM_EFFECT -> donation("SUBSCRIPTION_RENEWED", SOOPDonationType.SUBSCRIPTION_RENEWED, get(f, 0), get(f, 1), get(f, 2), num(f, 3), null);
            // case SOOPServiceCode.SVC_SENDSUBSCRIPTION -> donation("SUBSCRIPTION_GIFTED", SOOPDonationType.SUBSCRIBED, get(f, 5), get(f, 1), get(f, 2), 1, null);
            // case SOOPServiceCode.SVC_OGQ_EMOTICON_GIFT -> donation("OGQ_EMOTICON_GIFTED", SOOPDonationType.STICKER_GIFTED, null, get(f, 1), get(f, 2), 1, get(f, 5));
            // case SOOPServiceCode.SVC_GEM_ITEMSEND -> donation("GEM_GIFTED", SOOPDonationType.BALLOON_GIFTED, null, get(f, 1), get(f, 2), 1, get(f, 3));

            default -> null;
        };
    }

    private static @NotNull ParsedMessage donation(
            @NotNull String action, @NotNull SOOPDonationType type,
            @Nullable String bjId, @Nullable String userId, @Nullable String userNickname,
            int count, @Nullable String message) {
        return new ParsedMessage(action, new SOOPDonationMessage(type, bjId, userId, userNickname, count, message));
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
