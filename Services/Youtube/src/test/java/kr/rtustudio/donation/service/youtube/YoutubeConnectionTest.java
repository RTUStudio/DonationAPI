package kr.rtustudio.donation.service.youtube;

import kr.rtustudio.donation.common.Donation;
import kr.rtustudio.donation.service.ServiceHandler;
import kr.rtustudio.donation.service.youtube.configuration.YoutubeConfig;
import kr.rtustudio.donation.service.youtube.data.YoutubePlayer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class YoutubeConnectionTest {

    @Test
    @Disabled("수동으로 유튜브 핸들명(TEST_YOUTUBE_HANDLE)을 입력한 뒤 실행하세요.")
    public void testYoutubeDonationConnection() throws InterruptedException {
        // TODO: 라이브 중인 유튜브 핸들명을 입력하세요 (예: @침착맨)
        String TEST_YOUTUBE_HANDLE = "@"; 

        System.out.println("=== Youtube 실시간 후원/채팅 감지 테스트 시작 ===");

        YoutubeConfig config = new YoutubeConfig() {
            @Override public boolean isEnabled() { return true; }
            @Override public int getPollingIntervalMs() { return 5000; }
        };

        ServiceHandler<YoutubePlayer> handler = new ServiceHandler<>(
                (Donation donation) -> {
                    System.out.println("[Youtube 후원 수신] " + donation);
                },
                (YoutubePlayer player) -> {
                    System.out.println("[Youtube 연동 성공] " + player.channelId() + " (UUID: " + player.uuid() + ")");
                }
        );

        YoutubeService service = new YoutubeService(config, handler);
        service.start();

        UUID dummyUuid = UUID.randomUUID();
        // 구독자 등록 -> 내부적으로 polling 스케줄 등록됨
        service.reconnect(dummyUuid, new YoutubePlayer(dummyUuid, TEST_YOUTUBE_HANDLE));

        // 30초 대기 (실시간 확인)
        long startTime = System.currentTimeMillis();
        while(System.currentTimeMillis() - startTime < 30000) {
            Thread.sleep(1000);
        }

        service.close();
        System.out.println("=== Youtube 연동 테스트 종료 ===");
    }
}
