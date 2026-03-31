package kr.rtustudio.donation.common.net;

import kr.rtustudio.donation.common.configuration.SocketOption;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j(topic = "DonationAPI/WebSocket")
public abstract class WebSocketClient extends WebSocketListener {

    private static final OkHttpClient SHARED_HTTP_CLIENT;

    static {
        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(1024);
        dispatcher.setMaxRequestsPerHost(1024);
        SHARED_HTTP_CLIENT = new OkHttpClient.Builder()
                .dispatcher(dispatcher)
                .readTimeout(0, TimeUnit.MILLISECONDS)
                .build();
    }

    private final String url;
    private final SocketOption socketOption;
    private final Runnable successCallback;
    private final Runnable failureCallback;
    private final String clientName;

    private volatile WebSocket webSocket;
    private volatile boolean closed;
    private ScheduledExecutorService executor;
    private final AtomicInteger reconnectAttempts = new AtomicInteger(0);

    public WebSocketClient(String clientName, String url, SocketOption socketOption, Runnable successCallback, Runnable failureCallback) {
        this.clientName = clientName;
        this.url = url;
        this.socketOption = socketOption;
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
    }

    public void connect() {
        if (closed) return;

        Request.Builder builder = new Request.Builder().url(url);
        configureRequest(builder);

        this.webSocket = SHARED_HTTP_CLIENT.newWebSocket(builder.build(), this);
    }

    /**
     * 서브클래스에서 Request.Builder에 헤더 등을 추가할 수 있습니다.
     */
    protected void configureRequest(Request.Builder builder) {
    }

    @Override
    public void onOpen(@NotNull WebSocket webSocket, @NotNull Response response) {
        log.info("{} socket connected", clientName);
        reconnectAttempts.set(0);

        handleOpen(webSocket);

        if (successCallback != null) {
            successCallback.run();
        }
    }

    protected void handleOpen(WebSocket webSocket) {
        // Override if needed
    }

    @Override
    public abstract void onMessage(@NotNull WebSocket webSocket, @NotNull String text);

    @Override
    public void onClosed(@NotNull WebSocket webSocket, int code, @NotNull String reason) {
        log.info("{} socket closed: {} {}", clientName, code, reason);
        closeExecutors();
    }

    @Override
    public void onFailure(@NotNull WebSocket webSocket, @NotNull Throwable t, @Nullable Response response) {
        if (closed) return;

        int attempt = reconnectAttempts.incrementAndGet();

        if (socketOption == null || !socketOption.getReconnection().isEnabled()) {
            log.warn("{} socket failure: {}", clientName, t.getMessage());
            if (failureCallback != null) failureCallback.run();
            return;
        }

        int maxDelay = socketOption.getReconnection().getMaxDelay();
        int baseDelay = socketOption.getReconnection().getDelay();
        long delay = Math.min((long) baseDelay * attempt, maxDelay);

        log.warn("{} disconnected (Attempt {}). Reconnecting in {}ms... Error: {}", clientName, attempt, delay, t.getMessage());

        scheduleReconnect(delay);
    }

    private void scheduleReconnect(long delayMillis) {
        if (closed) return;
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor(r -> {
                Thread thread = new Thread(r, clientName + "-Reconnect");
                thread.setDaemon(true);
                return thread;
            });
        }
        executor.schedule(() -> {
            if (!closed) connect();
        }, delayMillis, TimeUnit.MILLISECONDS);
    }

    public void close() {
        closed = true;
        if (webSocket != null) {
            try {
                webSocket.close(1000, "Disconnected");
            } catch (Exception ignored) {}
            webSocket = null;
        }
        closeExecutors();
    }

    private void closeExecutors() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public WebSocket getWebSocket() {
        return webSocket;
    }
}
