package kr.rtustudio.donation.service.chzzk.official.net.http.factory.impl;

import com.google.common.collect.Maps;
import kr.rtustudio.donation.service.chzzk.official.net.http.executor.HttpRequestExecutor;
import kr.rtustudio.donation.service.chzzk.official.net.http.executor.okhttp.*;
import kr.rtustudio.donation.service.chzzk.official.net.http.factory.HttpRequestExecutorFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

public class HttpRequestExecutorFactoryImpl implements HttpRequestExecutorFactory {

    private final @NotNull Map<String, Class<? extends HttpRequestExecutor<?, ?, ?>>> types = Maps.newHashMap();

    {
        types.put("access_token_grant", AccessTokenGrantExecutor.class);
        types.put("access_token_refresh", AccessTokenRefreshExecutor.class);
        types.put("access_token_revoke", AccessTokenRevokeExecutor.class);
        types.put("authorization_code", AuthorizationCodeExecutor.class);
        types.put("category_search", CategorySearchExecutor.class);
        types.put("channel_information", ChannelInformationExecutor.class);
        types.put("chat_announcement_set", ChatAnnouncementSetExecutor.class);
        types.put("chat_message_send", ChatMessageSendExecutor.class);
        types.put("chat_settings_change", ChatSettingsChangeExecutor.class);
        types.put("chat_settings", ChatSettingsExecutor.class);
        types.put("live_search", LiveSearchExecutor.class);
        types.put("live_settings_change", LiveSettingsChangeExecutor.class);
        types.put("live_settings", LiveSettingsExecutor.class);
        types.put("live_stream_key", LiveStreamKeyExecutor.class);
        types.put("user_information", UserInformationExecutor.class);
        types.put("session_url", SessionUrlExecutor.class);
        types.put("chat_event_subscribe", ChatEventSubscribeExecutor.class);
        types.put("chat_event_unsubscribe", ChatEventUnsubscribeExecutor.class);
        types.put("donation_event_subscribe", DonationEventSubscribeExecutor.class);
        types.put("donation_event_unsubscribe", DonationEventUnsubscribeExecutor.class);
        types.put("session_search", SessionSearchExecutor.class);
    }

    @Override
    public <Request, Response, HttpClient> @NotNull Optional<HttpRequestExecutor<Request, Response, HttpClient>> create(@NotNull String type) {
        HttpRequestExecutor<Request, Response, HttpClient> executor;
        try {
            executor = createInstance(type);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(executor);
    }

    @SuppressWarnings("unchecked")
    private <Request, Response, HttpClient> @Nullable HttpRequestExecutor<Request, Response, HttpClient> createInstance(String type)
            throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<? extends HttpRequestExecutor<?, ?, ?>> clazz = types.get(type);
        if (clazz == null) {
            return null;
        }

        Constructor<?> constructor = clazz.getDeclaredConstructor();
        return (HttpRequestExecutor<Request, Response, HttpClient>) constructor.newInstance();
    }

}
