package kr.rtustudio.donation.service.toonation;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.toonation.configuration.ToonationConfig;
import kr.rtustudio.donation.service.toonation.data.ToonationPlayer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class ToonationConnectionTest {

    @Test
    @Disabled("수동으로 투네이션 AlertKey 등을 입력한 뒤 실행하세요.")
    public void testToonationDonationConnection() throws InterruptedException {
        // TODO: 본인의 투네이션 통합 위젯 링크에서 추출한 payload 키를 입력하세요.
        String TEST_TOONATION_PAYLOAD = "";

        System.out.println("=== Toonation 웹소켓 연동 테스트 시작 ===");

        ToonationConfig config = new ToonationConfig() {
            @Override public boolean isEnabled() { return true; }
            @Override public SocketOption getSocket() {
                return new SocketOption() {
                    @Override public int getTimeout() { return 10000; }
                    @Override public KeepaliveOption getKeepalive() {
                        return new KeepaliveOption() {
                            @Override public boolean isEnabled() { return false; }
                            @Override public int getInterval() { return 0; }
                        };
                    }
                    @Override public ReconnectionOption getReconnection() {
                        return new ReconnectionOption() {
                            @Override public boolean isEnabled() { return true; }
                            @Override public int getDelay() { return 1000; }
                            @Override public int getMaxDelay() { return 5000; }
                        };
                    }
                };
            }
        };

        ServiceHandler<ToonationPlayer> handler = new ServiceHandler<>(
                (Donation donation) -> {
                    System.out.println("[Toonation 후원 수신] " + donation);
                },
                (ToonationPlayer player) -> {
                    System.out.println("[Toonation 연동 성공] UUID: " + player.uuid());
                }
        );

        ToonationService service = new ToonationService(config, handler);
        service.start();

        UUID dummyUuid = UUID.randomUUID();
        // register
        service.reconnect(dummyUuid, new ToonationPlayer(dummyUuid, "test_channel", "test_alert_key", TEST_TOONATION_PAYLOAD));

        // 30초 대기
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 30000) {
            Thread.sleep(1000);
        }

        service.close();
        System.out.println("=== Toonation 연동 테스트 종료 ===");
    }
}
