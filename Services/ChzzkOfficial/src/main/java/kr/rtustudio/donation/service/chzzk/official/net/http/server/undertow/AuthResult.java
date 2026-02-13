package kr.rtustudio.donation.service.chzzk.official.net.http.server.undertow;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AuthResult(
        @NotNull String code,
        @NotNull String state,
        @NotNull Type type,
        @Nullable String user
) {

    public enum Type {
        INVALID_CODE_PARAMETER,
        INVALID_STATE_PARAMETER,
        SUCCESS
    }

}
