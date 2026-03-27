package kr.rtustudio.donation.service.soop;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.common.configuration.SocketOption;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.soop.configuration.SoopConfig;
import kr.rtustudio.donation.service.soop.data.SoopPlayer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;



public class SoopConnectionTest {

    @Test
    @Disabled("수동으로 SOOP API 클라이언트 키를 입력한 뒤 실행하세요.")
    public void testSOOPDonationConnection() throws InterruptedException {
        // TODO: 발급받은 SOOP OpenAPI 정보를 입력하세요 (테스트용)
        String TEST_CLIENT_ID = "YOUR_CLIENT_ID";
        String TEST_CLIENT_SECRET = "YOUR_CLIENT_SECRET";

        System.out.println("=== SOOP OpenAPI 연동 테스트 시작 ===");

        SoopConfig config = new SoopConfig() {
            @Override public boolean isEnabled() { return true; }
            @Override public String getClientId() { return TEST_CLIENT_ID; }
            @Override public String getClientSecret() { return TEST_CLIENT_SECRET; }
            @Override public String getBaseUri() { return "http://localhost:8080/login"; }
            @Override public String getHost() { return "127.0.0.1"; }
            @Override public int getPort() { return 8080; }
            @Override public SocketOption getSocket() {
                return new SocketOption() {
                    @Override public int getTimeout() { return 3000; }
                    @Override public KeepaliveOption getKeepalive() {
                        return new KeepaliveOption() {
                            @Override public boolean isEnabled() { return true; }
                            @Override public int getInterval() { return 60000; }
                        };
                    }
                    @Override public ReconnectionOption getReconnection() {
                        return new ReconnectionOption() {
                            @Override public boolean isEnabled() { return true; }
                            @Override public int getDelay() { return 1000; }
                            @Override public int getMaxDelay() { return 30000; }
                        };
                    }
                };
            }
        };

        ServiceHandler<SoopPlayer> handler = new ServiceHandler<>(
                (Donation donation) -> {
                    System.out.println("[SOOP 후원 수신] " + donation);
                },
                (SoopPlayer player) -> {
                    System.out.println("[SOOP 연동 성공] UUID: " + player.uuid() + " | SOOP ID: " + player.channelId());
                }
        );

        SoopService service = new SoopService(config, handler);
        service.start();

        System.out.println("[테스트 안내] ========================================");
        System.out.println("SOOP AuthServer가 http://127.0.0.1:8080/login 링크로 띄워졌습니다.");
        System.out.println("웹 브라우저로 위 주소에 접속해 로그인을 성공하면 이곳에 수신 로그가 뜹니다.");
        System.out.println("테스트는 60초 뒤에 자동으로 종료됩니다.");
        System.out.println("======================================================");

        // 60초 대기
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 60000) {
            Thread.sleep(1000);
        }

        service.close();
        System.out.println("=== SOOP OpenAPI 연동 테스트 종료 ===");
    }
}
