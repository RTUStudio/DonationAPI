package kr.rtustudio.donation.service.youtube.core;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ChatItem {
    protected ChatItemType type;
    protected String authorName;
    protected String authorChannelID;
    protected String message;
    protected List<Object> messageExtended;
    protected String authorIconURL;
    protected String id;
    protected long timestamp;
    protected List<AuthorType> authorType;
    protected String memberBadgeIconURL;
    //For paid message
    protected int bodyBackgroundColor;
    protected int bodyTextColor;
    protected int headerBackgroundColor;
    protected int headerTextColor;
    protected int authorNameTextColor;
    protected String purchaseAmount;
    //For paid sticker
    protected String stickerIconURL;
    protected int backgroundColor;
    //For ticker paid message
    protected int endBackgroundColor;
    protected int durationSec;
    protected int fullDurationSec;
    //If moderator enabled
    protected String contextMenuParams;
    protected String pinToTopParams;
    protected String chatDeleteParams; // can be executed by author too
    protected String timeBanParams;
    protected String userBanParams;
    protected String userUnbanParams;
    //Connected chat
    protected YouTubeLiveChat liveChat;

    protected ChatItem(YouTubeLiveChat liveChat) {
        this.authorType = new ArrayList<>();
        this.authorType.add(AuthorType.NORMAL);
        this.type = ChatItemType.MESSAGE;
        this.liveChat = liveChat;
    }

    public boolean isAuthorVerified() {
        return this.authorType.contains(AuthorType.VERIFIED);
    }

    public boolean isAuthorOwner() {
        return this.authorType.contains(AuthorType.OWNER);
    }

    public boolean isAuthorModerator() {
        return this.authorType.contains(AuthorType.MODERATOR);
    }

    public boolean isAuthorMember() {
        return this.authorType.contains(AuthorType.MEMBER);
    }

    @Override
    public String toString() {
        return "ChatItem{" +
                "type=" + type +
                ", authorName='" + authorName + '\'' +
                ", authorChannelID='" + authorChannelID + '\'' +
                ", message='" + message + '\'' +
                ", messageExtended=" + messageExtended +
                ", iconURL='" + authorIconURL + '\'' +
                ", id='" + id + '\'' +
                ", timestamp=" + timestamp +
                ", authorType=" + authorType +
                ", memberBadgeIconURL='" + memberBadgeIconURL + '\'' +
                ", bodyBackgroundColor=" + bodyBackgroundColor +
                ", bodyTextColor=" + bodyTextColor +
                ", headerBackgroundColor=" + headerBackgroundColor +
                ", headerTextColor=" + headerTextColor +
                ", authorNameTextColor=" + authorNameTextColor +
                ", purchaseAmount='" + purchaseAmount + '\'' +
                ", stickerIconURL='" + stickerIconURL + '\'' +
                ", backgroundColor=" + backgroundColor +
                ", endBackgroundColor=" + endBackgroundColor +
                ", durationSec=" + durationSec +
                ", fullDurationSec=" + fullDurationSec +
                '}';
    }
}
