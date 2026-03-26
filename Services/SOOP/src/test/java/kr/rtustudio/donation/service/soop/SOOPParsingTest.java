package kr.rtustudio.donation.service.soop;

import kr.rtustudio.donation.service.soop.data.SOOPDonationMessage;
import kr.rtustudio.donation.service.soop.data.SOOPDonationType;
import kr.rtustudio.donation.service.soop.net.socket.SOOPMessageParser;
import kr.rtustudio.donation.service.soop.net.socket.SOOPServiceCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SOOPParsingTest {

    @Test
    public void testBalloonGiftParsing() {
        // SOOP SDK: bjId=t[0], userId=t[1], userNickname=t[2], count=t[3]
        String[] mockFields = new String[]{"test_bj", "test_user", "테스터닉네임", "100", "0"};
        
        SOOPMessageParser.ParsedMessage parsed = SOOPMessageParser.parse(SOOPServiceCode.SVC_SENDBALLOON, mockFields);
        
        assertNotNull(parsed);
        assertEquals("BALLOON_GIFTED", parsed.action());
        
        assertTrue(parsed.message() instanceof SOOPDonationMessage);
        SOOPDonationMessage msg = (SOOPDonationMessage) parsed.message();
        
        assertEquals(SOOPDonationType.BALLOON_GIFTED, msg.donationType());
        assertEquals("test_bj", msg.bjId());
        assertEquals("test_user", msg.userId());
        assertEquals("테스터닉네임", msg.userNickname());
        assertEquals(100, msg.count());
    }

    @Test
    public void testAdBalloonParsing() {
        // SDK: bjId=t[1], userId=t[2], userNickname=t[3], count=t[9]
        String[] mockFields = new String[]{"", "test_bj", "test_user2", "시청자2", "", "", "", "", "", "10", ""};

        SOOPMessageParser.ParsedMessage parsed = SOOPMessageParser.parse(SOOPServiceCode.SVC_ADCON_EFFECT, mockFields);

        assertNotNull(parsed);
        assertEquals("ADBALLOON_GIFTED", parsed.action());

        assertTrue(parsed.message() instanceof SOOPDonationMessage);
        SOOPDonationMessage msg = (SOOPDonationMessage) parsed.message();

        assertEquals(SOOPDonationType.AD_BALLOON_GIFTED, msg.donationType());
        assertEquals("test_bj", msg.bjId());
        assertEquals("test_user2", msg.userId());
        assertEquals("시청자2", msg.userNickname());
        assertEquals(10, msg.count());
    }
}
