package kr.rtustudio.donation.service.chzzk.net.http.factory.impl;

import com.google.common.collect.Maps;
import kr.rtustudio.donation.service.chzzk.exception.HttpRequestExecutorNotFoundException;
import kr.rtustudio.donation.service.chzzk.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.net.http.executor.Requests;
import kr.rtustudio.donation.service.chzzk.net.http.executor.okhttp.*;
import kr.rtustudio.donation.service.chzzk.net.http.factory.HttpRequestExecutorFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

public class HttpRequestExecutorFactoryImpl implements HttpRequestExecutorFactory {

    private final @NotNull Map<Requests, Supplier<HttpRequestExecutor<?, ?, ?>>> types = Maps.newHashMap();

    {
        this.types.put(Requests.ACCESS_TOKEN_GRANT, AccessTokenGrantExecutor::new);
        this.types.put(Requests.ACCESS_TOKEN_REFRESH, AccessTokenRefreshExecutor::new);
        this.types.put(Requests.ACCESS_TOKEN_REVOKE, AccessTokenRevokeExecutor::new);
        this.types.put(Requests.AUTHORIZATION_CODE, AuthorizationCodeExecutor::new);
        this.types.put(Requests.CATEGORY_SEARCH, CategorySearchExecutor::new);
        this.types.put(Requests.CHANNEL_INFORMATION, ChannelInformationExecutor::new);
        this.types.put(Requests.CHANNEL_MANAGER, ChannelManagerExecutor::new);
        this.types.put(Requests.CHANNEL_FOLLOWER, ChannelFollowerExecutor::new);
        this.types.put(Requests.CHANNEL_SUBSCRIBER, ChannelSubscriberExecutor::new);
        this.types.put(Requests.CHAT_ANNOUNCEMENT_SET, ChatAnnouncementSetExecutor::new);
        this.types.put(Requests.CHAT_MESSAGE_SEND, ChatMessageSendExecutor::new);
        this.types.put(Requests.CHAT_SETTINGS_CHANGE, ChatSettingsChangeExecutor::new);
        this.types.put(Requests.CHAT_SETTINGS, ChatSettingsExecutor::new);
        this.types.put(Requests.LIVE_SEARCH, LiveSearchExecutor::new);
        this.types.put(Requests.LIVE_SETTINGS_CHANGE, LiveSettingsChangeExecutor::new);
        this.types.put(Requests.LIVE_SETTINGS, LiveSettingsExecutor::new);
        this.types.put(Requests.LIVE_STREAM_KEY, LiveStreamKeyExecutor::new);
        this.types.put(Requests.USER_INFORMATION, UserInformationExecutor::new);
        this.types.put(Requests.SESSION_URL, SessionUrlExecutor::new);
        this.types.put(Requests.CHAT_EVENT_SUBSCRIBE, ChatEventSubscribeExecutor::new);
        this.types.put(Requests.CHAT_EVENT_UNSUBSCRIBE, ChatEventUnsubscribeExecutor::new);
        this.types.put(Requests.DONATION_EVENT_SUBSCRIBE, DonationEventSubscribeExecutor::new);
        this.types.put(Requests.DONATION_EVENT_UNSUBSCRIBE, DonationEventUnsubscribeExecutor::new);
        this.types.put(Requests.SESSION_SEARCH, SessionSearchExecutor::new);
        this.types.put(Requests.RESTRICTION_ADD, RestrictionAddExecutor::new);
        this.types.put(Requests.RESTRICTION_DELETE, RestrictionDeleteExecutor::new);
        this.types.put(Requests.RESTRICTION_SEARCH, RestrictionSearchExecutor::new);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <Request, Response, HttpClient> @NotNull HttpRequestExecutor<Request, Response, HttpClient> create(@NotNull Requests type) {
        Supplier<HttpRequestExecutor<?, ?, ?>> supplier = this.types.get(type);
        if (supplier == null) {
            throw new HttpRequestExecutorNotFoundException(type, this);
        }
        return (HttpRequestExecutor<Request, Response, HttpClient>) supplier.get();
    }

}
