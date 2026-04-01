package kr.rtustudio.donation.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlatformTest {

    @Test
    public void testPlatformImmutability() {
        // 단위 불변성 테스트
        assertEquals("캐시", Platform.TOONATION.unit());
        assertEquals("별풍선", Platform.SOOP.unit());
        assertEquals("치즈", Platform.CHZZK.unit());
        assertEquals("빔", Platform.CIME.unit());
        assertEquals("원", Platform.YOUTUBE.unit());

        // 비율(1단위당 가치) 불변성 테스트
        assertEquals(1, Platform.TOONATION.rate());
        assertEquals(100, Platform.SOOP.rate()); // 별풍선 1개 = 100원
        assertEquals(1, Platform.CHZZK.rate());
        assertEquals(1, Platform.CIME.rate());
        assertEquals(1, Platform.YOUTUBE.rate());
    }

    @Test
    public void testDonationCalculation() {
        // Platform 비율을 통한 계산 로직이 정상 동작하는지 테스트
        Donation soopDonation = new Donation(java.util.UUID.randomUUID(), kr.rtustudio.donation.service.Services.SOOP, Platform.SOOP, DonationType.CHAT, "testId", "testName", "message", "", 15);
        assertEquals(1500, soopDonation.price()); // 15개 * 100 = 1500원

        Donation chzzkDonation = new Donation(java.util.UUID.randomUUID(), kr.rtustudio.donation.service.Services.CHZZK, Platform.CHZZK, DonationType.CHAT, "testId2", "testName2", "message2", "", 5000);
        assertEquals(5000, chzzkDonation.price()); // 5000치즈 * 1 = 5000원
    }
}
