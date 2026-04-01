package kr.rtustudio.donation.common.net;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j(topic = "DonationAPI/Polling")
public abstract class PollingClient {

    private final String clientName;
    private final long pollingIntervalMs;
    private ScheduledExecutorService executorService;
    private volatile boolean closed;

    public PollingClient(String clientName, long pollingIntervalMs) {
        this.clientName = clientName;
        this.pollingIntervalMs = pollingIntervalMs;
    }

    public void start() {
        if (closed || executorService != null) return;
        this.executorService = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r, clientName + "-Poller");
            t.setDaemon(true);
            return t;
        });

        this.executorService.scheduleWithFixedDelay(() -> {
            if (closed) return;
            try {
                poll();
            } catch (Exception e) {
                log.warn("{} polling error: {}", clientName, e.getMessage());
            }
        }, 0, pollingIntervalMs, TimeUnit.MILLISECONDS);

        log.info("{} started polling (interval: {}ms)", clientName, pollingIntervalMs);
    }

    protected abstract void poll() throws Exception;

    public void close() {
        closed = true;
        if (executorService != null) {
            executorService.shutdownNow();
            executorService = null;
            log.info("{} polling stopped", clientName);
        }
    }
}
