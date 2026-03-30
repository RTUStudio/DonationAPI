package kr.rtustudio.donation.service.chzzk.net.http.server;

import kr.rtustudio.donation.service.chzzk.net.http.server.undertow.AuthResult;
import org.jetbrains.annotations.NotNull;

public interface AuthServerHandler {

    default void onFailure(@NotNull AuthServer server, @NotNull AuthResult result) {
    }

    default boolean onSuccess(@NotNull AuthServer server, @NotNull AuthResult result) {
        return true;
    }

}
