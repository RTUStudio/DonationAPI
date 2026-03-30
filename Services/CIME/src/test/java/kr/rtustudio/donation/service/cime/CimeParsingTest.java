package kr.rtustudio.donation.service.cime;

import kr.rtustudio.donation.service.cime.net.CimeSocket;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class CimeParsingTest {

    @Test
    public void testCimeDonationParsing() {
        AtomicInteger parsedAmount = new AtomicInteger(0);
        AtomicReference<String> parsedNickname = new AtomicReference<>("");
        AtomicReference<String> parsedMessage = new AtomicReference<>("");
        AtomicBoolean parsedAnonymous = new AtomicBoolean(false);

        CimeSocket socket = new CimeSocket(
                "dummy_key",
                null,
                (amount, nickname, message, isAnonymous) -> {
                    parsedAmount.set(amount);
                    parsedNickname.set(nickname);
                    parsedMessage.set(message);
                    parsedAnonymous.set(isAnonymous);
                },
                () -> {},
                () -> {}
        );

        String jsonPayload = "{\"Attributes\":{\"extra\":\"{\\\"amt\\\":5000,\\\"msg\\\":\\\"테스트 후원입니다\\\",\\\"anon\\\":false,\\\"prof\\\":{\\\"ch\\\":{\\\"na\\\":\\\"테스터\\\"}}}\"}}";
        
        socket.onMessage(null, jsonPayload);

        assertEquals(5000, parsedAmount.get());
        assertEquals("테스터", parsedNickname.get());
        assertEquals("테스트 후원입니다", parsedMessage.get());
        assertFalse(parsedAnonymous.get());
    }

    @Test
    public void testCimeAnonymousParsing() {
        AtomicInteger parsedAmount = new AtomicInteger(0);
        AtomicReference<String> parsedNickname = new AtomicReference<>("");
        AtomicBoolean parsedAnonymous = new AtomicBoolean(false);

        CimeSocket socket = new CimeSocket(
                "dummy_key",
                null,
                (amount, nickname, message, isAnonymous) -> {
                    parsedAmount.set(amount);
                    parsedNickname.set(nickname);
                    parsedAnonymous.set(isAnonymous);
                },
                () -> {},
                () -> {}
        );

        String jsonPayload = "{\"amt\":1000,\"msg\":\"익명 후원\",\"anon\":true}";
        
        socket.onMessage(null, jsonPayload);

        assertEquals(1000, parsedAmount.get());
        assertEquals(true, parsedAnonymous.get());
    }
}
