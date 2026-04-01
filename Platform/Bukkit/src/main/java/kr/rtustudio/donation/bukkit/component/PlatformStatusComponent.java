package kr.rtustudio.donation.bukkit.component;

import kr.rtustudio.donation.service.Services;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class PlatformStatusComponent {

    private final Map<Services, Status> statuses = new ConcurrentHashMap<>();

    public void markConnected(Services service) {
        statuses.computeIfAbsent(service, k -> new Status()).setConnected(true);
    }

    public void markDisconnected(Services service) {
        Status status = statuses.get(service);
        if (status != null) {
            status.setConnected(false);
        }
    }

    public void resetDonationStatus(Services service) {
        Status status = statuses.get(service);
        if (status != null) {
            status.setDonationReceived(false);
        }
    }

    public void markDonationReceived(Services service) {
        Status status = statuses.get(service);
        if (status != null) {
            status.setDonationReceived(true);
        }
    }

    public boolean isActive(Services service) {
        Status status = statuses.get(service);
        return status != null && status.isConnected() && status.isDonationReceived();
    }

    @Data
    public static class Status {
        private boolean connected = false;
        private boolean donationReceived = false;
    }
}
