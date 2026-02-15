package kr.rtustudio.donation.service.soop.utils;

import io.undertow.server.HttpServerExchange;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.Map;

public class HttpExchangeQueryParameterParser {

    public static @Nullable String parse(@NotNull HttpServerExchange exchange, @NotNull String key) {
        Map<String, Deque<String>> queryParams = exchange.getQueryParameters();
        Deque<String> values = queryParams.get(key);
        if (values == null || values.isEmpty()) {
            return null;
        }
        return values.getFirst();
    }

}
