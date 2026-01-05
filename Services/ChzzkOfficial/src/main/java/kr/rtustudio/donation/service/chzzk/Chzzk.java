package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.service.chzzk.data.*;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandlerHolder;
import kr.rtustudio.donation.service.chzzk.impl.ChzzkBuilderImpl;
import kr.rtustudio.donation.service.chzzk.net.http.factory.HttpRequestExecutorFactory;
import kr.rtustudio.donation.service.chzzk.net.http.server.undertow.AuthResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Chzzk extends ChzzkEventHandlerHolder {

    static @NotNull ChzzkBuilder builder() {
        return new ChzzkBuilderImpl();
    }

    @NotNull String getClientId();

    @NotNull String getClientSecret();

    @NotNull HttpRequestExecutorFactory getHttpRequestExecutorFactory();

    @NotNull ChzzkSession getSession();

    @NotNull Optional<ChzzkToken> getToken();

    @NotNull CompletableFuture<Void> grantToken(@NotNull AuthResult authResult);

    @NotNull CompletableFuture<Void> grantToken(@NotNull String code, @NotNull String state);

    @NotNull CompletableFuture<Void> refreshToken();

    @NotNull CompletableFuture<Void> revokeToken();

    @NotNull CompletableFuture<Optional<ChzzkUser>> getCurrentUser();

    @NotNull CompletableFuture<Optional<ChzzkChannel>> getCurrentChannel();

    @NotNull CompletableFuture<Optional<ChzzkChannel>> getChannel(@NotNull String channelId);

    @NotNull CompletableFuture<List<ChzzkChannel>> getChannels(@NotNull Collection<String> channelIds);

    @NotNull CompletableFuture<List<ChzzkChannelManager>> getChannelManagers();

    @NotNull CompletableFuture<List<ChzzkChannelFollower>> getChannelFollowers(@Range(from = 0, to = Integer.MAX_VALUE) int page, @Range(from = 1, to = 50) int size);

    @NotNull CompletableFuture<List<ChzzkChannelSubscriber>> getChannelSubscribers(@Range(from = 0, to = Integer.MAX_VALUE) int page, @Range(from = 1, to = 50) int size, @NotNull ChzzkChannelSubscriberSort sort);

    @NotNull CompletableFuture<List<ChzzkCategorySearchResult>> searchCategories(@NotNull String categoryName, @Range(from = 1, to = 50) int amount);

    @NotNull CompletableFuture<List<ChzzkLiveSearchResult>> searchLiveStreams(@Range(from = 1, to = 50) int amount);

    @NotNull CompletableFuture<List<ChzzkLiveSearchResult>> searchLiveStreams(@Range(from = 1, to = 50) int amount, @NotNull String next);

    @NotNull CompletableFuture<Void> setChatAnnouncementByMessage(@NotNull String message);

    @NotNull CompletableFuture<Void> setChatAnnouncementById(@NotNull String messageId);

    @NotNull CompletableFuture<Optional<ChzzkChatMessageSendResult>> sendChatMessage(@NotNull String message);

    @NotNull CompletableFuture<Optional<ChzzkChatSettings>> getChatSettings();

    @NotNull CompletableFuture<Void> setChatSettings(@NotNull ChzzkChatSettings settings);

    @NotNull CompletableFuture<Optional<ChzzkLiveSettings>> getLiveSettings();

    @NotNull CompletableFuture<Void> setLiveSettings(@NotNull ChzzkLiveSettings settings);

    @NotNull CompletableFuture<Optional<ChzzkLiveStreamKey>> getLiveStreamKey();

    @NotNull CompletableFuture<Optional<ChzzkSessionUrl>> getSessionUrl();

    @NotNull CompletableFuture<List<ChzzkSessionSearchResult>> searchSessions(@Range(from = 1, to = 50) int amount, @Range(from = 0, to = Integer.MAX_VALUE) int page);

    @NotNull CompletableFuture<Void> subscribeChat(@NotNull String sessionKey);

    @NotNull CompletableFuture<Void> unsubscribeChat(@NotNull String sessionKey);

    @NotNull CompletableFuture<Void> subscribeDonation(@NotNull String sessionKey);

    @NotNull CompletableFuture<Void> unsubscribeDonation(@NotNull String sessionKey);

    @NotNull CompletableFuture<Void> restrictChannel(@NotNull String channelId);

    @NotNull CompletableFuture<Void> unrestrictChannel(@NotNull String channelId);

    @NotNull CompletableFuture<Optional<ChzzkRestrictionChannel>> getRestrictedChannel(@Range(from = 0, to = Integer.MAX_VALUE) int size);

    @NotNull CompletableFuture<Optional<ChzzkRestrictionChannel>> getRestrictedChannel(@Range(from = 0, to = Integer.MAX_VALUE) int size, @NotNull String next);

}
