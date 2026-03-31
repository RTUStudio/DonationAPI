package kr.rtustudio.donation.service.chzzk.impl;

import com.google.common.collect.ImmutableSet;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.chzzk.data.ChzzkChannel;
import kr.rtustudio.donation.service.chzzk.data.ChzzkSessionUrl;
import kr.rtustudio.donation.service.chzzk.data.ChzzkToken;
import kr.rtustudio.donation.service.chzzk.data.ChzzkUser;
import kr.rtustudio.donation.service.chzzk.net.data.*;
import kr.rtustudio.donation.service.chzzk.Chzzk;
import kr.rtustudio.donation.service.chzzk.ChzzkSession;
import kr.rtustudio.donation.service.chzzk.ChzzkTokenMutator;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandler;
import kr.rtustudio.donation.service.chzzk.event.ChzzkEventHandlerHolder;
import kr.rtustudio.donation.service.chzzk.exception.InvalidTokenException;
import kr.rtustudio.donation.service.chzzk.net.data.*;
import kr.rtustudio.donation.service.chzzk.net.http.client.ChzzkHttpClient;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.net.http.factory.HttpRequestExecutorFactory;
import kr.rtustudio.donation.service.chzzk.net.http.factory.impl.HttpRequestExecutorFactoryImpl;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;


@Getter
@Setter
@Slf4j(topic = "DonationAPI/CHZZK")
public class ChzzkImpl implements Chzzk, ChzzkTokenMutator, ChzzkEventHandlerHolder {

    private final @NotNull String clientId;
    private final @NotNull String clientSecret;
    private @Nullable ChzzkToken token;
    private final @Nullable SocketOption socketOption;

    private final @NotNull ImmutableSet<ChzzkEventHandler> handlers;

    private final @NotNull HttpRequestExecutorFactory httpRequestExecutorFactory;
    private final @NotNull ChzzkHttpClient<OkHttpClient> httpClient;

    private final @NotNull ChzzkSession session;

    ChzzkImpl(
            @NotNull String clientId, @NotNull String clientSecret,
            @Nullable ChzzkToken token, @Nullable SocketOption socketOption,
            @NotNull Set<ChzzkEventHandler> handlers
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.token = token;
        this.socketOption = socketOption;
        this.handlers = ImmutableSet.copyOf(handlers);
        this.httpRequestExecutorFactory = new HttpRequestExecutorFactoryImpl();
        this.httpClient = ChzzkHttpClient.okhttp();
        this.session = new ChzzkSessionImpl(this, socketOption);

        if (this.token != null) {
            handlers.forEach(handler -> handler.onGrantToken(this));
        }
    }

    public @NotNull Optional<ChzzkToken> getToken() {
        return Optional.ofNullable(token);
    }

    @Override
    public @NotNull CompletableFuture<Void> refreshTokenAsync() {
        return CompletableFuture.runAsync(this::refreshToken);
    }

    @Override
    public void refreshToken() {
        if (token == null) {
            throw new InvalidTokenException("Token cannot be null.");
        }

        Optional<HttpRequestExecutor<AccessTokenRefreshRequest, AccessTokenRefreshResponse, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("access_token_refresh");

        AccessTokenRefreshRequest requestInst = new AccessTokenRefreshRequest(token.refreshToken(), clientId, clientSecret);
        executor.map(it -> it.execute(httpClient, requestInst))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .ifPresentOrElse(
                        response -> {
                            this.token = new ChzzkToken(response.accessToken(), response.refreshToken());
                            log.info("Token refreshed successfully");
                        },
                        () -> log.warn("Token refresh API returned empty response, continuing with existing token")
                );

        handlers.forEach(handler -> handler.onRefreshToken(this));
    }

    @Override
    public @NotNull CompletableFuture<Void> revokeTokenAsync() {
        return CompletableFuture.runAsync(this::revokeToken);
    }

    @Override
    public void revokeToken() {
        if (token == null) {
            throw new InvalidTokenException("Token cannot be null.");
        }

        Optional<HttpRequestExecutor<AccessTokenRevokeRequest, Void, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("access_token_revoke");

        AccessTokenRevokeRequest accessTokenRevokeRequest = new AccessTokenRevokeRequest(
                clientId, clientSecret, token.accessToken(), AccessTokenRevokeRequest.TokenTypeHint.ACCESS_TOKEN);
        executor.map(it -> it.execute(httpClient, accessTokenRevokeRequest));

        AccessTokenRevokeRequest refreshTokenRevokeRequest = new AccessTokenRevokeRequest(
                clientId, clientSecret, token.refreshToken(), AccessTokenRevokeRequest.TokenTypeHint.REFRESH_TOKEN);
        executor.map(it -> it.execute(httpClient, refreshTokenRevokeRequest));

        token = null;

        handlers.forEach(handler -> handler.onRevokeToken(this));
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkUser>> getCurrentUserAsync() {
        return CompletableFuture.supplyAsync(this::getCurrentUser);
    }

    @Override
    public @NotNull Optional<ChzzkUser> getCurrentUser() {
        if (token == null) {
            throw new InvalidTokenException("Token cannot be null.");
        }

        Optional<HttpRequestExecutor<UserInformationRequest, UserInformationResponse, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("user_information");

        UserInformationRequest requestInst = new UserInformationRequest(token.accessToken());
        return executor
                .map(it -> it.execute(httpClient, requestInst))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ChzzkUser::of);
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkChannel>> getCurrentChannelAsync() {
        return CompletableFuture.supplyAsync(this::getCurrentChannel);
    }

    @Override
    public @NotNull Optional<ChzzkChannel> getCurrentChannel() {
        return getCurrentUser().flatMap(user -> getChannel(user.id()));
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkChannel>> getChannelAsync(@NotNull String channelId) {
        return CompletableFuture.supplyAsync(() -> getChannel(channelId));
    }

    @Override
    public @NotNull Optional<ChzzkChannel> getChannel(@NotNull String channelId) {
        Optional<HttpRequestExecutor<ChannelInformationRequest, ChannelInformationResponse, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("channel_information");

        ChannelInformationRequest requestInst = new ChannelInformationRequest(clientId, clientSecret, List.of(channelId));
        return executor
                .map(it -> it.execute(httpClient, requestInst))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(it -> !it.data().isEmpty())
                .map(it -> it.data().getFirst())
                .map(ChzzkChannel::of);
    }

    @Override
    public @NotNull CompletableFuture<List<ChzzkChannel>> getChannelsAsync(@NotNull Collection<String> channelIds) {
        return CompletableFuture.supplyAsync(() -> getChannels(channelIds));
    }

    @Override
    public @NotNull List<ChzzkChannel> getChannels(@NotNull Collection<String> channelIds) {
        Optional<HttpRequestExecutor<ChannelInformationRequest, ChannelInformationResponse, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("channel_information");

        ChannelInformationRequest requestInst = new ChannelInformationRequest(clientId, clientSecret, List.copyOf(channelIds));
        return executor
                .map(it -> it.execute(httpClient, requestInst))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(ChannelInformationResponse::data)
                .orElse(List.of())
                .stream()
                .map(ChzzkChannel::of)
                .toList();
    }

    @Override
    public @NotNull CompletableFuture<Optional<ChzzkSessionUrl>> getSessionUrlAsync() {
        return CompletableFuture.supplyAsync(this::getSessionUrl);
    }

    @Override
    public @NotNull Optional<ChzzkSessionUrl> getSessionUrl() {
        if (token == null) {
            throw new InvalidTokenException("Token cannot be null.");
        }

        Optional<HttpRequestExecutor<SessionUrlRequest, SessionUrlResponse, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("session_url");

        SessionUrlRequest requestInst = new SessionUrlRequest(token.accessToken());
        return executor
                .map(it -> it.execute(httpClient, requestInst))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(it -> new ChzzkSessionUrl(it.url()));
    }

    @Override
    public @NotNull CompletableFuture<Void> subscribeDonationAsync(@NotNull String sessionKey) {
        return CompletableFuture.runAsync(() -> subscribeDonation(sessionKey));
    }

    @Override
    public void subscribeDonation(@NotNull String sessionKey) {
        if (token == null) {
            throw new InvalidTokenException("Token cannot be null.");
        }

        Optional<HttpRequestExecutor<DonationEventSubscribeRequest, Void, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("donation_event_subscribe");

        DonationEventSubscribeRequest requestInst = new DonationEventSubscribeRequest(sessionKey, token.accessToken());
        executor.map(it -> it.execute(httpClient, requestInst));
    }

    @Override
    public @NotNull CompletableFuture<Void> unsubscribeDonationAsync(@NotNull String sessionKey) {
        return CompletableFuture.runAsync(() -> unsubscribeDonation(sessionKey));
    }

    @Override
    public void unsubscribeDonation(@NotNull String sessionKey) {
        if (token == null) {
            throw new InvalidTokenException("Token cannot be null.");
        }

        Optional<HttpRequestExecutor<DonationEventUnsubscribeRequest, Void, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("donation_event_unsubscribe");

        DonationEventUnsubscribeRequest requestInst = new DonationEventUnsubscribeRequest(sessionKey, token.accessToken());
        executor.map(it -> it.execute(httpClient, requestInst));
    }

    @Override
    public @NotNull CompletableFuture<Void> subscribeChatAsync(@NotNull String sessionKey) {
        return CompletableFuture.runAsync(() -> subscribeChat(sessionKey));
    }

    @Override
    public void subscribeChat(@NotNull String sessionKey) {
        if (token == null) {
            throw new InvalidTokenException("Token cannot be null.");
        }

        Optional<HttpRequestExecutor<ChatEventSubscribeRequest, Void, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("chat_event_subscribe");

        ChatEventSubscribeRequest requestInst = new ChatEventSubscribeRequest(sessionKey, token.accessToken());
        executor.map(it -> it.execute(httpClient, requestInst));
    }

    @Override
    public @NotNull CompletableFuture<Void> unsubscribeChatAsync(@NotNull String sessionKey) {
        return CompletableFuture.runAsync(() -> unsubscribeChat(sessionKey));
    }

    @Override
    public void unsubscribeChat(@NotNull String sessionKey) {
        if (token == null) {
            throw new InvalidTokenException("Token cannot be null.");
        }

        Optional<HttpRequestExecutor<ChatEventUnsubscribeRequest, Void, OkHttpClient>> executor =
                httpRequestExecutorFactory.create("chat_event_unsubscribe");

        ChatEventUnsubscribeRequest requestInst = new ChatEventUnsubscribeRequest(sessionKey, token.accessToken());
        executor.map(it -> it.execute(httpClient, requestInst));
    }

}
