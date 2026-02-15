package kr.rtustudio.donation.service.soop.utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import kr.rtustudio.donation.service.soop.exception.HttpResponseParseException;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Optional;

public class HttpResponseParser {

    public static <ResponseInst> Optional<ResponseInst> parse(@NotNull Response response, @NotNull TypeToken<ResponseInst> type) throws HttpResponseParseException {
        try {
            if (response.isSuccessful() && response.body() != null) {
                JsonObject responseJson = JsonParser.parseString(response.body().string()).getAsJsonObject();
                if (responseJson.has("data")) {
                    ResponseInst responseInst = Constants.GSON.fromJson(responseJson.get("data"), type.getType());
                    return Optional.of(responseInst);
                }
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new HttpResponseParseException("Cannot parse response. data=" + response, e);
        }
    }

    public static <ResponseInst> Optional<ResponseInst> parseFlat(@NotNull Response response, @NotNull TypeToken<ResponseInst> type) throws HttpResponseParseException {
        try {
            if (response.isSuccessful() && response.body() != null) {
                ResponseInst responseInst = Constants.GSON.fromJson(response.body().string(), type.getType());
                return Optional.of(responseInst);
            }
            return Optional.empty();
        } catch (IOException e) {
            throw new HttpResponseParseException("Cannot parse response. data=" + response, e);
        }
    }

}
