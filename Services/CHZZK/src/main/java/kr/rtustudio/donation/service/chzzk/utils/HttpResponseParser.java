package kr.rtustudio.donation.service.chzzk.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.chzzk.exception.HttpResponseParseException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

@Slf4j(topic = "DonationAPI/CHZZK")
public class HttpResponseParser {

    public static <ResponseInst> Optional<ResponseInst> parse(@NotNull Response response, @NotNull TypeToken<ResponseInst> type) throws HttpResponseParseException {
        try {
            String url = response.request().url().encodedPath();
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string();
                log.info("[HTTP] {} → {} ({}bytes)", url, response.code(), body.length());
                JsonObject responseJson = JsonParser.parseString(body).getAsJsonObject();
                if (responseJson.has("content")) {
                    ResponseInst responseInst = Constants.GSON.fromJson(responseJson.get("content"), type.getType());
                    return Optional.of(responseInst);
                }
                log.warn("[HTTP] {} → 'content' field missing. Response: {}", url, body.substring(0, Math.min(500, body.length())));
                return Optional.empty();
            }
            String errorBody = response.body() != null ? response.body().string() : "null";
            log.warn("[HTTP] {} → HTTP {} failed. Body: {}", url, response.code(), errorBody.substring(0, Math.min(500, errorBody.length())));
            return Optional.empty();
        } catch (IOException e) {
            throw new HttpResponseParseException("Cannot parse response. data=" + response, e);
        }
    }

}
