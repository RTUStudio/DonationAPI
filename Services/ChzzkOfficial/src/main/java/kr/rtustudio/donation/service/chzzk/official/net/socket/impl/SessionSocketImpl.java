package kr.rtustudio.donation.service.chzzk.official.net.socket.impl;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.socket.client.IO;
import io.socket.client.Socket;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.chzzk.official.net.data.message.*;
import kr.rtustudio.donation.service.chzzk.official.net.socket.SessionSocket;
import kr.rtustudio.donation.service.chzzk.official.net.socket.SessionSocketHandler;
import kr.rtustudio.donation.service.chzzk.official.utils.Constants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "DonationAPI/ChzzkOfficial")
@Getter
public class SessionSocketImpl implements SessionSocket {

    private static final OkHttpClient SHARED_HTTP_CLIENT;

    static {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(1000);
        dispatcher.setMaxRequestsPerHost(1000);
        SHARED_HTTP_CLIENT = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .readTimeout(1, TimeUnit.MINUTES)
                .build();
    }

    private final @NotNull String url;
    private final @NotNull SessionSocketHandler handler;

    private final @NotNull Socket socket;
    private @Nullable String sessionKey;

    public SessionSocketImpl(@NotNull String url, @NotNull SessionSocketHandler handler, @Nullable SocketOption socketOption) throws URISyntaxException {
        this.url = url;
        this.handler = handler;

        IO.Options options = new IO.Options();
        options.callFactory = SHARED_HTTP_CLIENT;
        options.webSocketFactory = SHARED_HTTP_CLIENT;
        options.forceNew = true;
        options.transports = new String[]{"websocket"};

        if (socketOption != null) {
            options.timeout = socketOption.getTimeout();
            options.reconnection = socketOption.isReconnectionEnabled();
            options.reconnectionAttempts = Integer.MAX_VALUE;
            options.reconnectionDelay = socketOption.getReconnectionDelay();
            options.reconnectionDelayMax = socketOption.getReconnectionMaxDelay();
        } else {
            options.timeout = 3000;
            options.reconnection = false;
        }

        socket = IO.socket(url, options);

        // 연결
        socket.on("connect", objects -> {
        });

        socket.on("connect_error", objects -> {
            log.error("Connect Error: {}", Arrays.toString(objects));
        });

        socket.on("disconnect", objects -> {
            log.warn("Disconnected: {}", Arrays.toString(objects));
        });

        // 이벤트 바인드
        socket.on("SYSTEM", objects -> {
            if (objects.length == 0) {
                handler.onInvalidResponse(this, objects);
                return;
            }

            String json = objects[0].toString();
            JsonElement element = JsonParser.parseString(json);
            JsonObject object = element.getAsJsonObject();

            if (!object.has("type")) {
                handler.onInvalidResponse(this, objects);
                return;
            }

            switch (object.get("type").getAsString().toLowerCase()) {
                case "connected" -> {
                    ConnectedMessage message = Constants.GSON.fromJson(element, ConnectedMessage.class);
                    this.sessionKey = message.data().sessionKey();
                    handler.onConnected(this, message);
                }
                case "subscribed" -> {
                    EventSubscribedMessage message = Constants.GSON.fromJson(element, EventSubscribedMessage.class);
                    handler.onEventSubscribed(this, message);
                }
                case "unsubscribed" -> {
                    EventUnsubscribedMessage message = Constants.GSON.fromJson(element, EventUnsubscribedMessage.class);
                    handler.onEventUnsubscribed(this, message);
                }
                case "revoked" -> {
                    EventRevokedMessage message = Constants.GSON.fromJson(element, EventRevokedMessage.class);
                    handler.onEventRevoked(this, message);
                }
            }
        });

        socket.on("CHAT", objects -> {
            if (objects.length == 0) {
                return;
            }

            String json = objects[0].toString();
            ChatMessage message = Constants.GSON.fromJson(json, ChatMessage.class);
            handler.onChatMessageReceived(this, message);
        });

        socket.on("DONATION", objects -> {
            if (objects.length == 0) {
                return;
            }

            String json = objects[0].toString();
            DonationMessage message = Constants.GSON.fromJson(json, DonationMessage.class);
            handler.onDonationMessageReceived(this, message);
        });
    }

    @Override
    public void connect() {
        socket.connect();
    }

    @Override
    public void disconnect() {
        socket.disconnect();
    }

    @Override
    public void ping() {
        if (socket.connected()) {
            socket.emit("ping");
        }
    }

    @Override
    public boolean isConnected() {
        return socket.connected();
    }

}
