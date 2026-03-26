package kr.rtustudio.donation.service.chzzk;

import com.google.gson.Gson;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationMessage;
import kr.rtustudio.donation.service.chzzk.data.ChzzkDonationType;
import kr.rtustudio.donation.service.chzzk.net.data.message.DonationMessage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ChzzkParsingTest {

    private static final Gson GSON = new Gson();

    @Test
    public void testChzzkDonationMessageParsing() {
        // 가상의 치지직 웹소켓 응답 JSON
        String mockChzzkJson = "{" +
                "\"donationType\":\"CHAT\"," +
                "\"channelId\":\"channel_123\"," +
                "\"donatorChannelId\":\"donator_456\"," +
                "\"donatorNickname\":\"치즈팬\"," +
                "\"payAmount\":\"15000\"," +
                "\"donationText\":\"테스트 후원입니다!\"," +
                "\"emojis\":{}" +
                "}";

        DonationMessage rawMessage = GSON.fromJson(mockChzzkJson, DonationMessage.class);
        assertNotNull(rawMessage);

        // ChzzkDonationMessage DTO 로 변환 테스트
        ChzzkDonationMessage parsedMessage = ChzzkDonationMessage.of(rawMessage);

        assertNotNull(parsedMessage);
        assertEquals(ChzzkDonationType.CHAT, parsedMessage.donationType());
        assertEquals("channel_123", parsedMessage.receiverChannelId());
        assertEquals("donator_456", parsedMessage.senderChannelId());
        assertEquals("치즈팬", parsedMessage.nickname());
        assertEquals("테스트 후원입니다!", parsedMessage.message());
        assertEquals(15000, parsedMessage.payAmount());
        assertEquals(0, parsedMessage.emojis().size());
    }
}
