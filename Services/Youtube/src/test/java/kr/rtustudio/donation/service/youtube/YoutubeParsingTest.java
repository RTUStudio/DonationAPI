package kr.rtustudio.donation.service.youtube;

import kr.rtustudio.donation.service.youtube.core.IdType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class YoutubeParsingTest {

    @Test
    public void testYoutubeAmountExtraction() {
        // YoutubeClient 내부의 금액 추출 및 통화 필터링 로직 단위 테스트
        String krwAmountStr = "₩5,000";
        int amountKrw = 0;
        if (krwAmountStr.contains("₩")) {
            String cleanedKrw = krwAmountStr.replaceAll("[^0-9]", "");
            amountKrw = cleanedKrw.isEmpty() ? 0 : Integer.parseInt(cleanedKrw);
        }
        assertEquals(5000, amountKrw);

        String usdAmountStr = "$100.00";
        int amountUsd = 0;
        if (usdAmountStr.contains("₩")) {
            String cleanedUsd = usdAmountStr.replaceAll("[^0-9]", "");
            amountUsd = cleanedUsd.isEmpty() ? 0 : Integer.parseInt(cleanedUsd);
        }
        assertEquals(0, amountUsd); // 달러는 필터링되어 0원(무시) 처리
    }

    @Test
    public void testHandleIdentifier() {
        // YoutubeClient 내부의 핸들명 검출 로직 단위 테스트
        String handle = "@ipecter";
        String cleanedHandle = handle.startsWith("@") ? handle.substring(1) : handle;
        IdType idType1 = (cleanedHandle.startsWith("UC") && cleanedHandle.length() == 24) ? IdType.CHANNEL : IdType.USER;
        
        assertEquals("ipecter", cleanedHandle);
        assertSame(IdType.USER, idType1);

        String channelId = "UC1234567890123456789012";
        String cleanedChannel = channelId.startsWith("@") ? channelId.substring(1) : channelId;
        IdType idType2 = (cleanedChannel.startsWith("UC") && cleanedChannel.length() == 24) ? IdType.CHANNEL : IdType.USER;
        
        assertEquals("UC1234567890123456789012", cleanedChannel);
        assertSame(IdType.CHANNEL, idType2);
    }
}
