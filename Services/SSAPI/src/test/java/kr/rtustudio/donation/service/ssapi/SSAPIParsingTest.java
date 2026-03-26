package kr.rtustudio.donation.service.ssapi;

import com.google.gson.Gson;
import kr.rtustudio.donation.service.ssapi.data.DonationData;
import kr.rtustudio.donation.service.ssapi.data.ResponseResult;
import org.junit.jupiter.api.Test;
import org.xerial.snappy.Snappy;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SSAPIParsingTest {

    private static final Gson GSON = new Gson();

    @Test
    public void testDonationDataSnappyParsing() throws IOException {
        String mockJson = "{" +
                "\"_id\":\"test_id\"," +
                "\"platform\":\"SOOP\"," +
                "\"type\":\"DONATION_CHAT\"," +
                "\"streamer_id\":\"fake_bj\"," +
                "\"user_id\":\"fake_user\"," +
                "\"nickname\":\"테스터\"," +
                "\"cnt\":1," +
                "\"message\":\"테스트 후원입니다\"," +
                "\"amount\":10000" +
                "}";

        // 압축
        byte[] compressed = Snappy.compress(mockJson);

        // SSAPIService 내부에서 실행되는 해제 루틴 검증
        String uncompressed = Snappy.uncompressString(compressed);
        DonationData donationData = GSON.fromJson(uncompressed, DonationData.class);

        assertNotNull(donationData);
        assertEquals("SOOP", donationData.platform());
        assertEquals("fake_bj", donationData.streamerId());
        assertEquals("테스터", donationData.nickname());
        assertEquals(10000, donationData.amount());
        assertEquals("테스트 후원입니다", donationData.message());
    }

    @Test
    public void testApiResultParsing() {
        String apiResponse = "{\"error\":0,\"message\":\"Successfully processed\"}";
        
        // ResponseResult는 error(int), message(String) 필드를 가짐
        ResponseResult result = GSON.fromJson(apiResponse, ResponseResult.class);
        
        assertNotNull(result);
        assertTrue(result.succeeded());
        assertEquals("Successfully processed", result.message());
    }
}
