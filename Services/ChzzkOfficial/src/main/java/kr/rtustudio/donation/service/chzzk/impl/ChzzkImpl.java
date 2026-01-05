package kr.rtustudio.donation.service.chzzk.impl;

import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.ChzzkSession;
import kr.rtustudio.donation.service.chzzk.data.*;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import kr.rtustudio.donation.service.chzzk.exception.ChzzkException;
import kr.rtustudio.donation.service.chzzk.exception.HttpRequestExecutionException;
import kr.rtustudio.donation.service.chzzk.exception.InvalidTokenException;
import kr.rtustudio.donation.service.chzzk.net.data.*;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.net.http.executor.Requests;
import kr.rtustudio.donation.service.chzzk.net.http.factory.HttpRequestExecutorFactory;
import kr.rtustudio.donation.service.chzzk.net.http.factory.impl.HttpRequestExecutorFactoryImpl;
import kr.rtustudio.donation.service.chzzk.net.http.server.undertow.AuthResult;
import kr.rtustudio.donation.service.chzzk.utils.Constants;
import lombok.Getter;
import lombok.Setter;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;


@Getter
@Setter
public class ChzzkImpl implements Chzzk {

    private final @NotNull String clientId;
    private final @NotNull String clientSecret;
    private final @NotNull ChzzkEventHandler handler;
    private final @NotNull HttpRequestExecutorFactory httpRequestExecutorFactory;
    private final @NotNull ChzzkHttpClient<OkHttpClient> httpClient;
    private final @NotNull ChzzkSession session;
    private @Nullable ChzzkToken token;

    ChzzkImpl(
            @NotNull String clientId, @NotNull String clientSecret,
            @Nullable ChzzkToken token, @NotNull ChzzkEventHandler handler
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.token = token;
        this.handler = handler;
        this.httpRequestExecutorFactory = new HttpRequestExecutorFactoryImpl();
        this.httpClient = ChzzkHttpClient.okhttp();
        this.session = new ChzzkSessionImpl(this);
    }

    public @NotNull Optional<ChzzkToken> getToken() {
        return Optional.ofNullable(this.token);
    }

    @NotNull
    @Override
    public CompletableFuture<Void> grantToken(@NotNull AuthResult authResult) {
        return grantToken(authResult.code(), authResult.state());
    }

    @NotNull
    @Override
    public CompletableFuture<Void> grantToken(@NotNull String code, @NotNull String state) {
        return CompletableFuture.runAsync(() -> {
            HttpRequestExecutor<AccessTokenGrantRequest, AccessTokenGrantResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.ACCESS_TOKEN_GRANT);

            AccessTokenGrantRequest request = new AccessTokenGrantRequest(this.clientId, this.clientSecret, code, state);
            try {
                executor.execute(this.httpClient, request).ifPresent(response -> {
                    this.token = new ChzzkToken(response.accessToken(), response.refreshToken());
                    this.handler.onGrantToken(this);
                });
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to grant token.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> refreshToken() {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to refresh token.");
            }

            HttpRequestExecutor<AccessTokenRefreshRequest, AccessTokenRefreshResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.ACCESS_TOKEN_REFRESH);

            AccessTokenRefreshRequest request = new AccessTokenRefreshRequest(this.token.refreshToken(), this.clientId, this.clientSecret);
            try {
                executor.execute(this.httpClient, request).ifPresent(response -> {
                    this.token = new ChzzkToken(response.accessToken(), response.refreshToken());
                    this.handler.onRefreshToken(this);
                });
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to refresh token.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> revokeToken() {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to revoke tokens.");
            }

            HttpRequestExecutor<AccessTokenRevokeRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.ACCESS_TOKEN_REVOKE);

            // Revoke Access Token
            AccessTokenRevokeRequest accessTokenRevokeRequest = new AccessTokenRevokeRequest(
                    this.clientId, this.clientSecret, this.token.accessToken(), AccessTokenRevokeRequest.TokenTypeHint.ACCESS_TOKEN);
            try {
                executor.execute(this.httpClient, accessTokenRevokeRequest);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to revoke access token.", e);
            }

            // Revoke Refresh Token
            AccessTokenRevokeRequest refreshTokenRevokeRequest = new AccessTokenRevokeRequest(
                    this.clientId, this.clientSecret, this.token.refreshToken(), AccessTokenRevokeRequest.TokenTypeHint.REFRESH_TOKEN);
            try {
                executor.execute(this.httpClient, refreshTokenRevokeRequest);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to revoke refresh token.", e);
            }

            this.token = null;
            this.handler.onRevokeToken(this);
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkUser>> getCurrentUser() {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to get current user.");
            }

            HttpRequestExecutor<UserInformationRequest, UserInformationResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.USER_INFORMATION);

            UserInformationRequest request = new UserInformationRequest(this.token.accessToken());
            try {
                return executor.execute(this.httpClient, request).map(response -> {
                    ChzzkUser user = ChzzkUser.of(response);
                    return Optional.of(user);
                }).orElse(Optional.empty());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get current user.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkChannel>> getCurrentChannel() {
        return CompletableFuture.supplyAsync(() -> {
            Optional<ChzzkUser> user = getCurrentUser().join();
            if (user.isEmpty()) {
                return Optional.empty();
            }
            return getChannel(user.get().id()).join();
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkChannel>> getChannel(@NotNull String channelId) {
        return CompletableFuture.supplyAsync(() -> {
            HttpRequestExecutor<ChannelInformationRequest, ChannelInformationResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHANNEL_INFORMATION);

            ChannelInformationRequest request = new ChannelInformationRequest(this.clientId, this.clientSecret, List.of(channelId));
            try {
                return executor.execute(this.httpClient, request).map(response ->
                        response.data().stream().findFirst().map(ChzzkChannel::of)
                ).orElse(Optional.empty());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get channel.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<List<ChzzkChannel>> getChannels(@NotNull Collection<String> channelIds) {
        return CompletableFuture.supplyAsync(() -> {
            HttpRequestExecutor<ChannelInformationRequest, ChannelInformationResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHANNEL_INFORMATION);

            ChannelInformationRequest request = new ChannelInformationRequest(this.clientId, this.clientSecret, List.copyOf(channelIds));
            try {
                return executor.execute(this.httpClient, request).map(response ->
                        response.data().stream().map(ChzzkChannel::of).toList()
                ).orElse(List.of());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get channels.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<List<ChzzkChannelManager>> getChannelManagers() {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to get channel managers.");
            }

            HttpRequestExecutor<ChannelManagerRequest, ChannelManagerResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHANNEL_MANAGER);

            ChannelManagerRequest request = new ChannelManagerRequest(this.token.accessToken());
            try {
                return executor.execute(this.httpClient, request).map(response ->
                        response.data().stream().map(ChzzkChannelManager::of).toList()
                ).orElse(List.of());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get channel managers.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<List<ChzzkChannelFollower>> getChannelFollowers(@Range(from = 0, to = Integer.MAX_VALUE) int page, @Range(from = 1, to = 50) int size) {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to get channel followers.");
            }

            HttpRequestExecutor<ChannelFollowerRequest, ChannelFollowerResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHANNEL_FOLLOWER);

            ChannelFollowerRequest request = new ChannelFollowerRequest(page, size, this.token.accessToken());
            try {
                return executor.execute(this.httpClient, request).map(response ->
                        response.data().stream().map(ChzzkChannelFollower::of).toList()
                ).orElse(List.of());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get channel followers.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<List<ChzzkChannelSubscriber>> getChannelSubscribers(@Range(from = 0, to = Integer.MAX_VALUE) int page, @Range(from = 1, to = 50) int size, @NotNull ChzzkChannelSubscriberSort sort) {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to get channel followers.");
            }

            HttpRequestExecutor<ChannelSubscriberRequest, ChannelSubscriberResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHANNEL_SUBSCRIBER);

            ChannelSubscriberRequest request = new ChannelSubscriberRequest(page, size, sort.toString(), this.token.accessToken());
            try {
                return executor.execute(this.httpClient, request).map(response ->
                        response.data().stream().map(ChzzkChannelSubscriber::of).toList()
                ).orElse(List.of());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get channel subscribers.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<List<ChzzkCategorySearchResult>> searchCategories(
            @NotNull String categoryName, @Range(from = 1, to = 50) int amount) {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to search categories.");
            }

            HttpRequestExecutor<CategorySearchRequest, CategorySearchResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHANNEL_INFORMATION);

            CategorySearchRequest request = new CategorySearchRequest(amount, categoryName, this.token.accessToken());
            try {
                return executor.execute(this.httpClient, request).map(response ->
                        response.data().stream().map(ChzzkCategorySearchResult::of).toList()
                ).orElse(List.of());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to search categories.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<List<ChzzkLiveSearchResult>> searchLiveStreams(@Range(from = 1, to = 50) int amount) {
        return searchLiveStreams(amount, "");
    }

    @Override
    public @NotNull CompletableFuture<List<ChzzkLiveSearchResult>> searchLiveStreams(@Range(from = 1, to = 50) int amount, @NotNull String next) {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to search live streams.");
            }

            HttpRequestExecutor<LiveSearchRequest, LiveSearchResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.LIVE_SEARCH);

            LiveSearchRequest request = new LiveSearchRequest(amount, next, this.token.accessToken());
            try {
                return executor.execute(this.httpClient, request).map(response ->
                        response.data().stream().map(it -> ChzzkLiveSearchResult.of(it, response.page())).toList()
                ).orElse(List.of());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to search live streams.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> setChatAnnouncementByMessage(@NotNull String message) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to set chat announcement by message.");
            }

            HttpRequestExecutor<ChatAnnouncementSetRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHANNEL_INFORMATION);

            ChatAnnouncementSetRequest request = new ChatAnnouncementSetRequest(message, "", this.token.accessToken());
            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to set chat announcement by message.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> setChatAnnouncementById(@NotNull String messageId) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to set chat announcement by id.");
            }

            HttpRequestExecutor<ChatAnnouncementSetRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHANNEL_INFORMATION);

            ChatAnnouncementSetRequest request = new ChatAnnouncementSetRequest("", messageId, this.token.accessToken());
            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to set chat announcement by id.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkChatMessageSendResult>> sendChatMessage(@NotNull String message) {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to send chat message.");
            }

            HttpRequestExecutor<ChatMessageSendRequest, ChatMessageSendResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHAT_MESSAGE_SEND);

            ChatMessageSendRequest request = new ChatMessageSendRequest(message, this.token.accessToken());
            try {
                return executor.execute(this.httpClient, request).map(response -> {
                    ChzzkChatMessageSendResult result = ChzzkChatMessageSendResult.of(response.messageId(), message);
                    return Optional.of(result);
                }).orElse(Optional.empty());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to send chat message.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkChatSettings>> getChatSettings() {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to get chat settings.");
            }

            HttpRequestExecutor<ChatSettingsRequest, ChatSettingsResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHAT_SETTINGS);

            ChatSettingsRequest request = new ChatSettingsRequest(this.token.accessToken());
            try {
                return executor.execute(this.httpClient, request).map(response -> {
                    ChzzkChatSettings settings = ChzzkChatSettings.of(response);
                    return Optional.of(settings);
                }).orElse(Optional.empty());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get chat settings.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> setChatSettings(@NotNull ChzzkChatSettings settings) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to set chat settings.");
            }

            HttpRequestExecutor<ChatSettingsChangeRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHAT_SETTINGS_CHANGE);

            ChatSettingsChangeRequest request = new ChatSettingsChangeRequest(
                    settings.chatAvailableCondition().toString(),
                    settings.chatAvailableGroup().toString(),
                    settings.minimumFollowerTimeInMinutes(),
                    settings.isSubscriberAllowedInFollowerMode(),
                    this.token.accessToken()
            );

            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to set chat settings.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkLiveSettings>> getLiveSettings() {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to get live settings.");
            }

            HttpRequestExecutor<LiveSettingsRequest, LiveSettingsResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.LIVE_SETTINGS);

            LiveSettingsRequest request = new LiveSettingsRequest(this.token.accessToken());

            try {
                return executor.execute(this.httpClient, request).map(response -> {
                    ChzzkLiveSettings settings = ChzzkLiveSettings.of(response);
                    return Optional.of(settings);
                }).orElse(Optional.empty());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get live settings.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> setLiveSettings(@NotNull ChzzkLiveSettings settings) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to set live settings.");
            }

            HttpRequestExecutor<LiveSettingsChangeRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.LIVE_SETTINGS_CHANGE);

            LiveSettingsChangeRequest request = new LiveSettingsChangeRequest(
                    settings.liveTitle(), settings.categoryType().toString(),
                    settings.categoryId(), settings.tags(),
                    this.token.accessToken()
            );

            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to set live settings.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkLiveStreamKey>> getLiveStreamKey() {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to get live stream key.");
            }

            HttpRequestExecutor<LiveStreamKeyRequest, LiveStreamKeyResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.LIVE_STREAM_KEY);

            LiveStreamKeyRequest request = new LiveStreamKeyRequest(this.token.accessToken());

            try {
                return executor.execute(this.httpClient, request).map(response -> {
                    ChzzkLiveStreamKey streamKey = ChzzkLiveStreamKey.of(response);
                    return Optional.of(streamKey);
                }).orElse(Optional.empty());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get live stream key.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkSessionUrl>> getSessionUrl() {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to get session url.");
            }

            HttpRequestExecutor<SessionUrlRequest, SessionUrlResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.SESSION_URL);

            SessionUrlRequest request = new SessionUrlRequest(this.token.accessToken());

            try {
                return executor.execute(this.httpClient, request).map(response -> {
                    ChzzkSessionUrl url = new ChzzkSessionUrl(response.url());
                    return Optional.of(url);
                }).orElse(Optional.empty());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get session url.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<List<ChzzkSessionSearchResult>> searchSessions(@Range(
            from = 1, to = 50) int amount, @Range(from = 0, to = Integer.MAX_VALUE) int page) {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to search sessions.");
            }

            HttpRequestExecutor<SessionSearchRequest, SessionSearchResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.SESSION_SEARCH);

            SessionSearchRequest request = new SessionSearchRequest(amount, Integer.toString(page), this.token.accessToken());

            try {
                return executor.execute(this.httpClient, request).map(response ->
                        response.data().stream().map(ChzzkSessionSearchResult::of).toList()
                ).orElse(List.of());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to search sessions.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> subscribeChat(@NotNull String sessionKey) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to subscribe chat.");
            }

            HttpRequestExecutor<ChatEventSubscribeRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHAT_EVENT_SUBSCRIBE);

            ChatEventSubscribeRequest request = new ChatEventSubscribeRequest(sessionKey, this.token.accessToken());

            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to subscribe chat.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> unsubscribeChat(@NotNull String sessionKey) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to unsubscribe chat.");
            }

            HttpRequestExecutor<ChatEventUnsubscribeRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.CHAT_EVENT_UNSUBSCRIBE);

            ChatEventUnsubscribeRequest request = new ChatEventUnsubscribeRequest(sessionKey, this.token.accessToken());

            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to unsubscribe chat.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> subscribeDonation(@NotNull String sessionKey) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to subscribe donation.");
            }

            HttpRequestExecutor<DonationEventSubscribeRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.DONATION_EVENT_SUBSCRIBE);

            DonationEventSubscribeRequest request = new DonationEventSubscribeRequest(sessionKey, this.token.accessToken());
            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to subscribe donation.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> unsubscribeDonation(@NotNull String sessionKey) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to unsubscribe donation.");
            }

            HttpRequestExecutor<DonationEventUnsubscribeRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.DONATION_EVENT_UNSUBSCRIBE);

            DonationEventUnsubscribeRequest request = new DonationEventUnsubscribeRequest(sessionKey, this.token.accessToken());

            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to unsubscribe donation.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> restrictChannel(@NotNull String channelId) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to add restriction channel.");
            }

            HttpRequestExecutor<RestrictionAddRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.RESTRICTION_ADD);

            RestrictionAddRequest request = new RestrictionAddRequest(channelId, this.token.accessToken());
            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to add restriction channel.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Void> unrestrictChannel(@NotNull String channelId) {
        return CompletableFuture.runAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to remove restriction channel.");
            }

            HttpRequestExecutor<RestrictionDeleteRequest, Void, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.RESTRICTION_DELETE);

            RestrictionDeleteRequest request = new RestrictionDeleteRequest(channelId, this.token.accessToken());
            try {
                executor.execute(this.httpClient, request);
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to remove restriction channel.", e);
            }
        }, Constants.EXECUTOR);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkRestrictionChannel>> getRestrictedChannel(@Range(from = 0, to = Integer.MAX_VALUE) int size) {
        return getRestrictedChannel(size, "");
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkRestrictionChannel>> getRestrictedChannel(@Range(from = 0, to = Integer.MAX_VALUE) int size, @NotNull String next) {
        return CompletableFuture.supplyAsync(() -> {
            if (this.token == null) {
                throw new InvalidTokenException("Failed to get restricted channel.");
            }

            HttpRequestExecutor<RestrictionSearchRequest, RestrictionSearchResponse, OkHttpClient> executor =
                    this.httpRequestExecutorFactory.create(Requests.RESTRICTION_SEARCH);

            RestrictionSearchRequest request = new RestrictionSearchRequest(size, next, this.token.accessToken());
            try {
                return executor.execute(this.httpClient, request).map(response -> {
                    ChzzkRestrictionChannel channel = ChzzkRestrictionChannel.of(response);
                    return Optional.of(channel);
                }).orElse(Optional.empty());
            } catch (HttpRequestExecutionException e) {
                throw new ChzzkException("Failed to get restricted channel.", e);
            }
        }, Constants.EXECUTOR);
    }

}
