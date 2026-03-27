package kr.rtustudio.donation.service.soop;

import kr.rtustudio.donation.service.soop.data.SoopDonationMessage;
import kr.rtustudio.donation.service.soop.data.SoopDonationType;
import kr.rtustudio.donation.service.soop.net.socket.SoopMessageParser;
import kr.rtustudio.donation.service.soop.net.socket.SoopServiceCode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SoopParsingTest {

    @Test
    public void testBalloonGiftParsing() {
        // SOOP SDK: bjId=t[0], userId=t[1], userNickname=t[2], count=t[3]
        String[] mockFields = new String[]{"test_bj", "test_user", "테스터닉네임", "100", "0"};
        
        SoopMessageParser.ParsedMessage parsed = SoopMessageParser.parse(SoopServiceCode.SVC_SENDBALLOON, mockFields);
        
        assertNotNull(parsed);
        assertEquals("BALLOON_GIFTED", parsed.action());
        
        assertTrue(parsed.message() instanceof SoopDonationMessage);
        SoopDonationMessage msg = (SoopDonationMessage) parsed.message();
        
        assertEquals(SoopDonationType.BALLOON_GIFTED, msg.donationType());
        assertEquals("test_bj", msg.bjId());
        assertEquals("test_user", msg.userId());
        assertEquals("테스터닉네임", msg.userNickname());
        assertEquals(100, msg.count());
    }

    @Test
    public void testAdBalloonParsing() {
        // SDK: bjId=t[1], userId=t[2], userNickname=t[3], count=t[9]
        String[] mockFields = new String[]{"", "test_bj", "test_user2", "시청자2", "", "", "", "", "", "10", ""};

        SoopMessageParser.ParsedMessage parsed = SoopMessageParser.parse(SoopServiceCode.SVC_ADCON_EFFECT, mockFields);

        assertNotNull(parsed);
        assertEquals("ADBALLOON_GIFTED", parsed.action());

        assertTrue(parsed.message() instanceof SoopDonationMessage);
        SoopDonationMessage msg = (SoopDonationMessage) parsed.message();

        assertEquals(SoopDonationType.AD_BALLOON_GIFTED, msg.donationType());
        assertEquals("test_bj", msg.bjId());
        assertEquals("test_user2", msg.userId());
        assertEquals("시청자2", msg.userNickname());
        assertEquals(10, msg.count());
    }
}
