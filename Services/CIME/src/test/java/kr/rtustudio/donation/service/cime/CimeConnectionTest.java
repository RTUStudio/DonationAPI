package kr.rtustudio.donation.service.cime;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.cime.configuration.CimeConfig;
import kr.rtustudio.donation.service.cime.data.CimePlayer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class CimeConnectionTest {

    @Test
    @Disabled("수동으로 씨미 AlertKey(TEST_CIME_ALERT_KEY)를 입력한 뒤 실행하세요.")
    public void testCimeDonationConnection() throws InterruptedException {
        // TODO: 본인의 씨미 위젯 링크에서 추출한 AlertKey 32자리를 입력하세요. (예: 1234abcd...)
        String TEST_CIME_ALERT_KEY = "";

        System.out.println("=== Cime 웹소켓 연동 테스트 시작 ===");

        CimeConfig config = new CimeConfig() {
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

        ServiceHandler<CimePlayer> handler = new ServiceHandler<>(
                (Donation donation) -> {
                    System.out.println("[Cime 후원 수신] " + donation);
                },
                (CimePlayer player) -> {
                    System.out.println("[Cime 연동 완료] " + player.alertKey() + " (UUID: " + player.uuid() + ")");
                }
        );

        CimeService service = new CimeService(config, handler);
        service.start();

        UUID dummyUuid = UUID.randomUUID();
        // register 
        service.reconnect(dummyUuid, new CimePlayer(dummyUuid, "test_channel", TEST_CIME_ALERT_KEY, "test_channel"));

        // 30초 대기
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 30000) {
            Thread.sleep(1000);
        }

        service.close();
        System.out.println("=== Cime 연동 테스트 종료 ===");
    }
}
