package kr.rtustudio.donation.service.toonation;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ToonationParsingTest {

    private static final Gson GSON = new Gson();

    @Test
    public void testToonationPayloadParsing() {
        String mockPayloadMessage = "{\"content\":{\"account\":\"user123\",\"name\":\"테오\",\"amount\":10000,\"message\":\"응원합니다\"}}";

        JsonObject json = GSON.fromJson(mockPayloadMessage, JsonObject.class);
        JsonObject content = json.getAsJsonObject("content");

        String id = content.has("account") ? content.get("account").getAsString() : "unknown";
        String name = content.has("name") ? content.get("name").getAsString() : "익명";
        long amount = content.has("amount") ? content.get("amount").getAsLong() : 0L;
        String message = content.has("message") ? content.get("message").getAsString() : "";

        assertEquals("user123", id);
        assertEquals("테오", name);
        assertEquals(10000L, amount);
        assertEquals("응원합니다", message);
    }
}
