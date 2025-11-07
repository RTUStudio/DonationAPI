package kr.rtustudio.donation.common;

import com.google.gson.Gson;
import io.socket.client.IO;
import io.socket.client.Socket;
import kr.rtustudio.donation.common.configuration.SocketConfig;
import kr.rtustudio.donation.common.data.Donation;
import kr.rtustudio.donation.common.data.Platform;
import kr.rtustudio.donation.common.data.ResponseResult;
import kr.rtustudio.donation.common.data.RoomInfo;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j(topic = "DonationAPI")
public class DonationAPI {

    private static final Gson GSON = new Gson();

    private static final String SOCKET_URL = "https://socket.ssapi.kr/";

    private static final String REST_URL = "https://api.ssapi.kr/";
    private static final String ROOM = "room/";
    private static final String USER = ROOM + "user/";
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    private transient final String bearerToken;
    private final Consumer<Donation> donationHandler;

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(10))
            .readTimeout(Duration.ofSeconds(15))
            .build();

    private final Socket socket;
    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private final ExecutorService httpExecutor = Executors.newCachedThreadPool();
    private volatile boolean loginFailed = false;

    public DonationAPI(String bearerToken, SocketConfig socketConfig, Consumer<Donation> donationHandler) {
        Objects.requireNonNull(bearerToken, "bearerToken is required");
        Objects.requireNonNull(socketConfig, "socket config is required");
        this.bearerToken = bearerToken;
        this.donationHandler = donationHandler;
        IO.Options opts = new IO.Options();
        opts.transports = new String[]{"websocket"};
        opts.timeout = socketConfig.getTimeout();
        opts.reconnection = socketConfig.isReconnectionEnabled();
        opts.reconnectionAttempts = Integer.MAX_VALUE;
        opts.reconnectionDelay = socketConfig.getReconnectionDelay();
        opts.reconnectionDelayMax = socketConfig.getReconnectionMaxDelay();

        try {
            this.socket = IO.socket(SOCKET_URL, opts);
        } catch (URISyntaxException e) {
            log.error("socket uri error: {}", e.getMessage());
            throw new RuntimeException(e);
        }

        socket.on(Socket.EVENT_CONNECT, args -> {
            log.info("소켓 연결됨");
            loginFailed = true;
            emitLogin();
        });

        socket.on(Socket.EVENT_DISCONNECT, args -> {
            log.info("소켓 연결 끊김");
            loginFailed = true;
        });

        socket.on("login", args -> loginFailed = false);

        socket.on("donation", args -> {
            try {
                if (args != null && args.length > 0) {
                    if (args[0] instanceof byte[] compressed) {
                        String json = Snappy.uncompressString(compressed);
                        Donation donation = GSON.fromJson(json, Donation.class);
                        if (this.donationHandler != null) {
                            this.donationHandler.accept(donation);
                        } else log.error("파싱 실패: {}", json);
                    } else log.error("캐스팅 실패: {}", Arrays.toString(args));
                } else log.error("비어있음: {}", Arrays.toString(args));
            } catch (Exception e) {
                log.error("donation handle error", e);
            }
        });

        socket.connect();

        int retryDelay = socketConfig.getLoginRetryDelay();
        executor.scheduleAtFixedRate(() -> {
            if (socket.connected() && loginFailed) emitLogin();
        }, retryDelay, retryDelay, TimeUnit.MILLISECONDS);

        if (socketConfig.isKeepaliveEnabled()) {
            int interval = socketConfig.getKeepaliveInterval();
            executor.scheduleAtFixedRate(() -> {
                if (socket.connected()) socket.emit("ping");
            }, 0L, interval, TimeUnit.MILLISECONDS);
        }
    }

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

    public CompletableFuture<ResponseResult> register(Platform platform, String user) {
        return CompletableFuture.supplyAsync(() -> {
            if (platform == null || user == null || user.isBlank()) {
                return new ResponseResult(1, "invalid arguments");
            }
            HttpUrl url = HttpUrl.parse(REST_URL + USER);
            if (url == null) return new ResponseResult(1, "invalid url");

            String json = String.format("{\"platform\":\"%s\",\"user\":\"%s\"}", platform.lowercase(), user);
            RequestBody body = RequestBody.create(json, JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .put(body)
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .addHeader("Accept", "application/json")
                    .build();

            try (okhttp3.Response resp = client.newCall(request).execute()) {
                int code = resp.code();
                String payload = resp.body() != null ? resp.body().string() : "";
                if (code < 200 || code >= 300) {
                    String msg = extractApiMessage(payload);
                    log.error("register failed HTTP {} {}: {}", code, url, msg.isEmpty() ? payload : msg);
                    return new ResponseResult(code, msg.isEmpty() ? payload : msg);
                }
                ResponseResult api = parseApiResponse(payload);
                if (api != null) {
                    if (api.error() == 0) return api;
                    log.error("register failed API {} {}: {}", code, url, api.message());
                    return api;
                }
                return new ResponseResult(0, "");
            } catch (IOException e) {
                log.error("register request error {} | error= {}", url, e.getMessage());
                return new ResponseResult(1, e.getMessage());
            }
        }, httpExecutor);
    }

    public CompletableFuture<ResponseResult> unregister(Platform platform, String user) {
        return CompletableFuture.supplyAsync(() -> {
            if (platform == null || user == null || user.isBlank()) {
                return new ResponseResult(1, "invalid arguments");
            }
            HttpUrl url = HttpUrl.parse(REST_URL + USER);
            if (url == null) return new ResponseResult(1, "invalid url");

            String json = String.format("{\"platform\":\"%s\",\"user\":\"%s\"}", platform.lowercase(), user);
            RequestBody body = RequestBody.create(json, JSON);

            Request request = new Request.Builder()
                    .url(url)
                    .delete(body)
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .addHeader("Accept", "application/json")
                    .build();

            try (okhttp3.Response resp = client.newCall(request).execute()) {
                int code = resp.code();
                String payload = resp.body() != null ? resp.body().string() : "";
                if (code < 200 || code >= 300) {
                    String msg = extractApiMessage(payload);
                    log.error("unregister failed HTTP {} {}: {}", code, url, msg.isEmpty() ? payload : msg);
                    return new ResponseResult(code, msg.isEmpty() ? payload : msg);
                }
                ResponseResult api = parseApiResponse(payload);
                if (api != null) {
                    if (api.error() == 0) return api;
                    log.error("unregister failed API {} {}: {}", code, url, api.message());
                    return api;
                }
                return new ResponseResult(0, "");
            } catch (IOException e) {
                log.error("unregister request error {} | error= {}", url, e.getMessage());
                return new ResponseResult(1, e.getMessage());
            }
        }, httpExecutor);
    }

    public CompletableFuture<RoomInfo> info() {
        return CompletableFuture.supplyAsync(() -> {
            HttpUrl url = HttpUrl.parse(REST_URL + ROOM);
            if (url == null) return null;

            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Authorization", "Bearer " + bearerToken)
                    .addHeader("Accept", "application/json")
                    .build();

            try (okhttp3.Response resp = client.newCall(request).execute()) {
                int code = resp.code();
                String payload = resp.body() != null ? resp.body().string() : "";
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
            } catch (IOException e) {
                log.error("info request error {} | error= {}", url, e.getMessage());
                return null;
            }
        }, httpExecutor);
    }

    public void close() {
        executor.shutdownNow();
        httpExecutor.shutdownNow();
        if (socket != null) {
            if (socket.connected()) socket.emit("logout");
            socket.disconnect();
            socket.close();
        }
    }

    private void emitLogin() {
        if (socket == null) return;
        socket.emit("login", bearerToken);
        socket.emit("setReceiver", "login,donation,status");
    }

    // private static final String DONATIONS_POLLING = "donations/polling/";
    // private String cursor;
    // public void poll() {
    //     if (bearerToken == null || bearerToken.isEmpty()) return;
    //     PollingResponse response = fetch();
    //     if (response != null) {
    //         this.cursor = response.getNextCursor();
    //     }
    // }
    // private PollingResponse fetch() {
    //     HttpUrl buildUrl = HttpUrl.parse(BASE_URL + DONATIONS_POLLING);
    //     if (buildUrl == null) return null;
    //     HttpUrl.Builder urlBuilder = buildUrl.newBuilder();
    //     if (cursor != null && !cursor.isEmpty()) {
    //         urlBuilder.addQueryParameter("cursor", cursor);
    //     }
    //     HttpUrl url = urlBuilder.build();
    //
    //     Request request = new Request.Builder()
    //             .url(url)
    //             .get()
    //             .addHeader("Authorization", "Bearer " + bearerToken)
    //             .addHeader("Accept", "application/json")
    //             .build();
    //
    //     try (okhttp3.Response resp = client.newCall(request).execute()) {
    //         int code = resp.code();
    //         ResponseBody body = resp.body();
    //         String payload = body != null ? body.string() : "";
    //
    //         if (code < 200 || code >= 300) {
    //             log.error("HTTP {} calling {}: {}", code, url, payload);
    //             return null;
    //         }
    //
    //         try {
    //             PollingResponse dto = GSON.fromJson(payload, PollingResponse.class);
    //             if (dto == null) {
    //                 log.error("Empty body parsed for {}", url);
    //                 return null;
    //             }
    //             if (this.cursor == null || this.cursor.isEmpty()) {
    //                 this.cursor = dto.getNextCursor();
    //                 return null;
    //             } else {
    //                 this.cursor = dto.getNextCursor();
    //                 return dto;
    //             }
    //         } catch (JsonSyntaxException jse) {
    //             log.error("Failed to parse JSON from {}: {} | error= {}", url, payload, jse.getMessage());
    //             return null;
    //         }
    //     } catch (IOException ioe) {
    //         log.error("Request failed: {} | error= {}", url, ioe.getMessage());
    //         return null;
    //     }
    // }

}
