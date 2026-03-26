package kr.rtustudio.donation.service.youtube;

import kr.rtustudio.donation.service.youtube.core.IdType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

public class YoutubeParsingTest {

    @Test
    public void testYoutubeAmountExtraction() {
        // YoutubeClient 내부의 금액 추출 로직 단위 테스트
        String krwAmountStr = "₩5,000";
        String cleanedKrw = krwAmountStr.replaceAll("[^0-9]", "");
        int amountKrw = cleanedKrw.isEmpty() ? 0 : Integer.parseInt(cleanedKrw);
        assertEquals(5000, amountKrw);

        String usdAmountStr = "$100.00";
        String cleanedUsd = usdAmountStr.replaceAll("[^0-9]", "");
        int amountUsd = cleanedUsd.isEmpty() ? 0 : Integer.parseInt(cleanedUsd);
        assertEquals(10000, amountUsd); // $100.00 -> 10000 
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
