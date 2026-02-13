package kr.rtustudio.donation.service.ssapi;

import com.google.gson.Gson;
import io.socket.client.IO;
import io.socket.client.Socket;
import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.common.Response;
import kr.rtustudio.donation.service.AbstractService;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.ssapi.configuration.SSAPIConfig;
import kr.rtustudio.donation.service.ssapi.data.DonationData;
import kr.rtustudio.donation.service.ssapi.data.ResponseResult;
import kr.rtustudio.donation.service.ssapi.data.RoomInfo;
import kr.rtustudio.donation.service.ssapi.data.SSPlayer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.function.Consumer;

@Slf4j(topic = "DonationAPI/SSAPI")
public class SSAPIService extends AbstractService<SSPlayer> {

    private static final Gson GSON = new Gson();
    private static final String SOCKET_URL = "https://socket.ssapi.kr/";
    private static final String REST_URL = "https://api.ssapi.kr/";
    private static final String ROOM = "room/";
    private static final String USER = ROOM + "user/";
    private static final String JSON = "application/json; charset=utf-8";

    @Getter
    private final SSAPIConfig config;

    public SSAPIService(SSAPIConfig config, Consumer<Donation> donationHandler, Consumer<SSPlayer> registerHandler) {
        super(donationHandler, registerHandler);
        this.config = config;
    }

    private HttpClient client;
    private Socket socket;
    private ScheduledExecutorService executor;
    private ExecutorService httpExecutor;
    private volatile boolean loginFailed = false;

    private static ResponseResult parseApiResponse(String payload) {
        try {
            return GSON.fromJson(payload, ResponseResult.class);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String extractApiMessage(String payload) {
        ResponseResult api = parseApiResponse(payload);
        return api != null && api.message() != null ? api.message() : "";
    }

    @Override
    public Services getType() {
        return Services.SSAPI;
    }

    @Override
    public void start() {
        if (!config.isEnabled()) return;
        this.client =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        this.executor = Executors.newSingleThreadScheduledExecutor();
        this.httpExecutor = Executors.newCachedThreadPool();

        IO.Options opts = new IO.Options();
        opts.transports = new String[]{"websocket"};
        opts.timeout = config.getSocket().getTimeout();
        opts.reconnection = config.getSocket().isReconnectionEnabled();
        opts.reconnectionAttempts = Integer.MAX_VALUE;
        opts.reconnectionDelay = config.getSocket().getReconnectionDelay();
        opts.reconnectionDelayMax = config.getSocket().getReconnectionMaxDelay();

        try {
            this.socket = IO.socket(SOCKET_URL, opts);
        } catch (URISyntaxException e) {
            log.error("socket uri error: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            log.info("Socket connected");
            if (args.length > 0) log.info(Arrays.toString(args));
            loginFailed = true;
            emitLogin();
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            log.info("Socket disconnected");
            loginFailed = true;
        });

        socket.on("login", args -> loginFailed = false);

        socket.on("donation", args -> {
            try {
                if (args != null && args.length > 0) {
                    if (args[0] instanceof byte[] compressed) {
                        String json = Snappy.uncompressString(compressed);
                        DonationData donationData = GSON.fromJson(json, DonationData.class);
                        if (getDonationHandler() != null) {
                            Donation donation = toDonation(donationData);
                            if (donation != null) getDonationHandler().accept(donation);
                        } else log.error("parse failed: {}", json);
                    } else log.error("cast failed: {}", Arrays.toString(args));
                } else log.error("empty payload: {}", Arrays.toString(args));
            } catch (Exception e) {
                log.error("donation handle error", e);
            }
        });

        socket.connect();

        int retryDelay = config.getLoginRetryDelay();
        executor.scheduleAtFixedRate(() -> {
            if (socket.connected() && loginFailed) emitLogin();
        }, retryDelay, retryDelay, TimeUnit.MILLISECONDS);

        if (config.getSocket().isKeepaliveEnabled()) {
            int interval = config.getSocket().getKeepaliveInterval();
            executor.scheduleAtFixedRate(() -> {
                if (socket.connected()) socket.emit("ping");
            }, 0L, interval, TimeUnit.MILLISECONDS);
        }
    }

    public CompletableFuture<Response> register(Platform platform, String user) {
        return sendUserRequest("PUT", platform, user);
    }

    public CompletableFuture<Response> unregister(Platform platform, String user) {
        return sendUserRequest("DELETE", platform, user);
    }

    public CompletableFuture<RoomInfo> info() {
        if (!config.isEnabled()) return CompletableFuture.completedFuture(null);
        return CompletableFuture.supplyAsync(() -> {
            URI url;
            try {
                url = URI.create(REST_URL + ROOM);
            } catch (IllegalArgumentException e) {
                return null;
            }

            HttpRequest request =
                    HttpRequest.newBuilder(url)
                            .timeout(Duration.ofSeconds(15))
                            .header("Authorization", "Bearer " + config.getApiKey())
                            .header("Accept", "application/json")
                            .GET()
                            .build();

            try {
                HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
                int code = resp.statusCode();
                String payload = resp.body() != null ? resp.body() : "";
                if (code < 200 || code >= 300) {
                    log.error("info failed HTTP {} {}: {}", code, url, payload);
                    return null;
                }
                RoomInfo info = GSON.fromJson(payload, RoomInfo.class);
                if (info == null) {
                    log.error("info parse failed for {}: empty body", url);
                    return null;
                }
                return info;
            } catch (IOException | InterruptedException e) {
                log.error("info request error {} | error= {}", url, e.getMessage());
                Thread.currentThread().interrupt();
                return null;
            }
        }, httpExecutor);
    }

    @Override
    public void close() {
        if (executor != null) executor.shutdownNow();
        if (httpExecutor != null) httpExecutor.shutdownNow();
        if (socket != null) {
            if (socket.connected()) socket.emit("logout");
            socket.disconnect();
            socket.close();
        }
    }

    private Donation toDonation(DonationData data) {
        if (data == null) return null;
        Platform platform = parsePlatform(data.platform());
        if (platform == null) {
            log.error("unknown platform in donation: {}", data.platform());
            return null;
        }
        return new Donation(
                null,
                getType(),
                platform,
                DonationType.CHAT,
                data.streamerId(),
                data.userId(),
                data.nickname(),
                data.message(),
                data.amount()
        );
    }

    private Platform parsePlatform(String value) {
        if (value == null) return null;
        try {
            return Platform.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    private void emitLogin() {
        if (socket == null) return;
        socket.emit("login", config.getApiKey());
        socket.emit("setReceiver", "login,donation,status");
    }

    private Response toResult(ResponseResult response) {
        return response != null && response.succeeded() ? Response.SUCCESS : Response.FAILURE;
    }

    private CompletableFuture<Response> disabled() {
        return CompletableFuture.completedFuture(Response.FAILURE);
    }

    private CompletableFuture<Response> sendUserRequest(String method, Platform platform, String user) {
        if (!config.isEnabled()) return disabled();
        return CompletableFuture.supplyAsync(() -> doUserRequest(method, platform, user), httpExecutor)
                .thenApply(this::toResult);
    }

    private ResponseResult doUserRequest(String method, Platform platform, String user) {
        if (platform == null || user == null || user.isBlank()) {
            return new ResponseResult(1, "invalid arguments");
        }
        URI url;
        try {
            url = URI.create(REST_URL + USER);
        } catch (IllegalArgumentException e) {
            return new ResponseResult(1, "invalid url");
        }

        String json = String.format("{\"platform\":\"%s\",\"user\":\"%s\"}", platform.name().toLowerCase(), user);

        HttpRequest request =
                HttpRequest.newBuilder(url)
                        .timeout(Duration.ofSeconds(15))
                        .header("Authorization", "Bearer " + config.getApiKey())
                        .header("Accept", "application/json")
                        .header("Content-Type", JSON)
                        .method(method, HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                        .build();

        try {
            HttpResponse<String> resp = client.send(request, HttpResponse.BodyHandlers.ofString());
            int code = resp.statusCode();
            String payload = resp.body() != null ? resp.body() : "";
            if (code < 200 || code >= 300) {
                String msg = extractApiMessage(payload);
                log.error("{} failed HTTP {} {}: {}", method.toLowerCase(), url, code, msg.isEmpty() ? payload : msg);
                return new ResponseResult(code, msg.isEmpty() ? payload : msg);
            }
            ResponseResult api = parseApiResponse(payload);
            if (api != null) {
                if (api.error() == 0) return api;
                log.error("{} failed API {} {}: {}", method.toLowerCase(), url, code, api.message());
                return api;
            }
            return new ResponseResult(0, "");
        } catch (IOException | InterruptedException e) {
            log.error("{} request error {} | error= {}", method.toLowerCase(), url, e.getMessage());
            Thread.currentThread().interrupt();
            return new ResponseResult(1, e.getMessage());
        }
    }
}
