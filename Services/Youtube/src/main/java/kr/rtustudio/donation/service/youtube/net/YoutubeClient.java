package kr.rtustudio.donation.service.youtube.net;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.common.net.PollingClient;
import kr.rtustudio.donation.service.youtube.YoutubeService;
import kr.rtustudio.donation.service.youtube.core.ChatItem;
import kr.rtustudio.donation.service.youtube.core.IdType;
import kr.rtustudio.donation.service.youtube.core.YouTubeLiveChat;
import kr.rtustudio.donation.service.youtube.data.YoutubePlayer;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class YoutubeClient extends PollingClient {

    private final YoutubeService service;
    private final Set<UUID> subscribers = ConcurrentHashMap.newKeySet();
    private final String handle;
    private final YouTubeLiveChat chat;

    public YoutubeClient(YoutubeService service, String handle, long pollingIntervalMs) throws Exception {
        super("Youtube-" + handle, pollingIntervalMs);
        this.service = service;
        this.handle = handle;

        String cleanedHandle = handle.startsWith("@") ? handle.substring(1) : handle;
        IdType idType = (cleanedHandle.startsWith("UC") && cleanedHandle.length() == 24) ? IdType.CHANNEL : IdType.USER;
        this.chat = new YouTubeLiveChat(cleanedHandle, true, idType);
    }

    public void addSubscriber(UUID uuid) {
        subscribers.add(uuid);
        String cleanedHandle = handle.startsWith("@") ? handle.substring(1) : handle;
        if (service.getHandler().success() != null) {
            service.getHandler().success().accept(new YoutubePlayer(uuid, cleanedHandle));
        }
    }

    public void removeSubscriber(UUID uuid) {
        subscribers.remove(uuid);
    }

    public int getSubscribersCount() {
        return subscribers.size();
    }

    @Override
    protected void poll() throws Exception {
        chat.update();
        for (ChatItem item : chat.getChatItems()) {
            handleChatItem(item);
        }
    }

    private void handleChatItem(ChatItem item) {
        if (subscribers.isEmpty() || service.getHandler().donation() == null) return;

        int amount = 0;
        boolean isDonation = false;

        switch (item.getType()) {
            case PAID_MESSAGE, PAID_STICKER, TICKER_PAID_MESSAGE -> {
                String rawAmount = item.getPurchaseAmount();
                // 한국 원화(₩) 후원만 처리 (타 국가 통화 무시)
                if (rawAmount != null && rawAmount.contains("₩")) {
                    String amountStr = rawAmount.replaceAll("[^0-9]", "");
                    if (!amountStr.isEmpty()) {
                        amount = Integer.parseInt(amountStr);
                        isDonation = true;
                    }
                }
            }
            case NEW_MEMBER_MESSAGE -> {
                isDonation = true;
            }
            default -> {}
        }

        if (isDonation) {
            String channelId = item.getAuthorChannelID() != null ? item.getAuthorChannelID() : "unknown";
            
            for (UUID subUuid : subscribers) {
                Donation donation = new Donation(
                        subUuid,
                        service.getType(),
                        Platform.YOUTUBE,
                        DonationType.CHAT,
                        "",
                        channelId,
                        item.getAuthorName(),
                        item.getMessage(),
                        amount
                );
                service.getHandler().donation().accept(donation);
            }
        }
    }
}
