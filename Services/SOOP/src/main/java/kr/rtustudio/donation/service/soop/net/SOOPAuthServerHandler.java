package kr.rtustudio.donation.service.soop.net;

import org.jetbrains.annotations.NotNull;

public interface SOOPAuthServerHandler {

    default void onFailure(@NotNull AuthResult result) {
    }

    default boolean onSuccess(@NotNull AuthResult result) {
        return true;
    }

}
