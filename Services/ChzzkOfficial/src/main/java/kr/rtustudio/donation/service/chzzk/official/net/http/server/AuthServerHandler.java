package kr.rtustudio.donation.service.chzzk.official.net.http.server;

import kr.rtustudio.donation.service.chzzk.official.net.http.server.undertow.AuthResult;
import org.jetbrains.annotations.NotNull;

public interface AuthServerHandler {

    default void onFailure(@NotNull AuthServer server, @NotNull AuthResult result) {
    }

    default void onSuccess(@NotNull AuthServer server, @NotNull AuthResult result) {
    }

}
