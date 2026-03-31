package kr.rtustudio.donation.service.cime;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.DonationType;
import kr.rtustudio.donation.common.Platform;
import kr.rtustudio.donation.service.Services;
import kr.rtustudio.donation.service.cime.data.CimePlayer;
import kr.rtustudio.donation.service.cime.net.CimeSocket;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Cime 구독자
 * <p>
 * 플레이어별 독립 WebSocket 세션을 관리합니다.
 */
@Slf4j(topic = "DonationAPI/CIME")
public class CimeSubscriber {

    private final CimeService service;
    private final Map<String, CimeSocket> activeSockets = new ConcurrentHashMap<>();
    private final Map<UUID, String> playerToAlertKey = new ConcurrentHashMap<>();

    public CimeSubscriber(CimeService service) {
        this.service = service;
    }

    /**
     * 플레이어를 등록하고 WebSocket 연결합니다.
     *
     * @param uuid     플레이어 UUID
     * @param alertKey 후원 알림 키
     * @return 등록 성공 여부
     */
    public boolean register(@NotNull UUID uuid, @NotNull String alertKey) {
        String existingKey = playerToAlertKey.get(uuid);
        if (existingKey != null) {
            if (existingKey.equals(alertKey)) return true;
            disconnect(uuid);
        }

        playerToAlertKey.put(uuid, alertKey);

        CimeSocket socket = activeSockets.computeIfAbsent(alertKey, key -> {
            CimeSocket newSocket = new CimeSocket(
                    key,
                    service.getConfig().getSocket(),
                    (amount, nickname, message, isAnonymous) -> {
                        String donatorName = isAnonymous ? "익명의 후원자" : nickname;
                        CimeSocket s = activeSockets.get(key);
                        if (s != null && service.getHandler() != null && service.getHandler().donation() != null) {
                            for (UUID subUuid : s.getSubscribers()) {
                                Donation donation = new Donation(
                                        subUuid,
                                        Services.CIME,
                                        Platform.CIME,
                                        DonationType.CHAT,
                                        key,
                                        donatorName,
                                        donatorName,
                                        message,
                                        amount
                                );
                                service.getHandler().donation().accept(donation);
                            }
                        }
                    },
                    () -> {
                        // 첫 연결 시
                        CimeSocket s = activeSockets.get(key);
                        if (s != null && service.getHandler() != null) {
                            for (UUID subUuid : s.getSubscribers()) {
                                if (service.getHandler().success() != null) {
                                    service.getHandler().success().accept(new CimePlayer(subUuid, key, key));
                                }
                                if (service.getHandler().messenger() != null) {
                                    service.getHandler().messenger().send(subUuid, "connection.activated", null);
                                }
                            }
                        }
                    },
                    () -> {
                        // 실패/종료 시 구독자 전원에게 실패 전파
                        CimeSocket s = activeSockets.get(key);
                        if (s != null && service.getHandler() != null && service.getHandler().failure() != null) {
                            for (UUID subUuid : s.getSubscribers()) {
                                service.getHandler().failure().accept(subUuid);
                            }
                        }
                    }
            );
            newSocket.connect();
            log.debug("Started new CIME WebSocket for alertKey: {}", key);
            return newSocket;
        });

        socket.addSubscriber(uuid);
        log.debug("Registered CIME subscriber for alertKey: {} (UUID: {})", alertKey, uuid);

        if (service.getHandler() != null && service.getHandler().messenger() != null) {
            service.getHandler().messenger().send(uuid, "connection.trying", null);
        }

        if (socket.isConnected()) {
            if (service.getHandler() != null && service.getHandler().success() != null) {
                service.getHandler().success().accept(new CimePlayer(uuid, alertKey, alertKey));
            }
            if (service.getHandler() != null && service.getHandler().messenger() != null) {
                service.getHandler().messenger().send(uuid, "connection.activated", null);
            }
        }

        return true;
    }

    /**
     * 플레이어의 WebSocket 연결을 해제합니다.
     *
     * @param uuid 플레이어 UUID
     */
    public void disconnect(@NotNull UUID uuid) {
        String alertKey = playerToAlertKey.remove(uuid);
        if (alertKey != null) {
            CimeSocket socket = activeSockets.get(alertKey);
            if (socket != null) {
                socket.removeSubscriber(uuid);
                if (socket.getSubscribersCount() == 0) {
                    socket.close();
                    activeSockets.remove(alertKey);
                    log.debug("Stopped CIME WebSocket for alertKey: {} (no more subscribers)", alertKey);
                }
            }
            log.debug("Disconnected CIME subscriber for UUID: {}", uuid);
        }
    }

    /**
     * 모든 세션을 종료합니다.
     */
    public void closeAll() {
        activeSockets.values().forEach(CimeSocket::close);
        activeSockets.clear();
        playerToAlertKey.clear();
    }
}
