package kr.rtustudio.donation.service.chzzk;

import kr.rtustudio.donation.service.chzzk.data.ChzzkChannel;
import kr.rtustudio.donation.service.chzzk.data.ChzzkSessionUrl;
import kr.rtustudio.donation.service.chzzk.data.ChzzkToken;
import kr.rtustudio.donation.service.chzzk.data.ChzzkUser;
import kr.rtustudio.donation.service.chzzk.data.*;
import kr.rtustudio.donation.service.chzzk.impl.ChzzkBuilderImpl;
import kr.rtustudio.donation.service.chzzk.net.http.factory.HttpRequestExecutorFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface Chzzk {

    static @NotNull ChzzkBuilder builder() {
        return new ChzzkBuilderImpl();
    }

    @NotNull String getClientId();

    @NotNull String getClientSecret();

    @NotNull HttpRequestExecutorFactory getHttpRequestExecutorFactory();

    @NotNull Optional<ChzzkToken> getToken();

    @NotNull CompletableFuture<Void> refreshTokenAsync();

    void refreshToken();

    @NotNull CompletableFuture<Void> revokeTokenAsync();

    void revokeToken();

    @NotNull CompletableFuture<Optional<ChzzkUser>> getCurrentUserAsync();

    @NotNull Optional<ChzzkUser> getCurrentUser();

    @NotNull CompletableFuture<Optional<ChzzkChannel>> getCurrentChannelAsync();

    @NotNull Optional<ChzzkChannel> getCurrentChannel();

    @NotNull CompletableFuture<Optional<ChzzkChannel>> getChannelAsync(@NotNull String channelId);

    @NotNull Optional<ChzzkChannel> getChannel(@NotNull String channelId);

    @NotNull CompletableFuture<List<ChzzkChannel>> getChannelsAsync(@NotNull Collection<String> channelIds);

    @NotNull List<ChzzkChannel> getChannels(@NotNull Collection<String> channelIds);

    @NotNull CompletableFuture<Optional<ChzzkSessionUrl>> getSessionUrlAsync();

    @NotNull Optional<ChzzkSessionUrl> getSessionUrl();

    @NotNull ChzzkSession getSession();

    @NotNull CompletableFuture<Void> subscribeChatAsync(@NotNull String sessionKey);

    void subscribeChat(@NotNull String sessionKey);

    @NotNull CompletableFuture<Void> unsubscribeChatAsync(@NotNull String sessionKey);

    void unsubscribeChat(@NotNull String sessionKey);

    @NotNull CompletableFuture<Void> subscribeDonationAsync(@NotNull String sessionKey);

    void subscribeDonation(@NotNull String sessionKey);

    @NotNull CompletableFuture<Void> unsubscribeDonationAsync(@NotNull String sessionKey);

    void unsubscribeDonation(@NotNull String sessionKey);

}
