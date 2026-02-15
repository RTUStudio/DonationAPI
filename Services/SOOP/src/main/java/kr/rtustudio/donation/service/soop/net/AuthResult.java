package kr.rtustudio.donation.service.soop.net;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record AuthResult(
        @NotNull String code,
        @NotNull Type type,
        @Nullable String user
) {

    public enum Type {
        INVALID_CODE_PARAMETER,
        SUCCESS
    }

}
